package com.fixnix.flow

import co.paralleluniverse.fibers.Suspendable
import com.fixnix.contract.WhistleContract
import com.fixnix.contract.WhistleContract.Companion.ID
import com.fixnix.modal.CreateWhistleFlowRequest
import com.fixnix.state.WhistleState
import net.corda.core.contracts.Command
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

object ComplaintCreateFlow {
    @InitiatingFlow //iniating
    @StartableByRPC//should wait for subflow
    class Initiator(val whistle:CreateWhistleFlowRequest):FlowLogic<SignedTransaction>()
    {
        companion object {
            object GENERATE_TRANSACTION: ProgressTracker.Step("Generating transaction based on new Complaint")
            object VERIFY_TRANSACTION: ProgressTracker.Step("Verifing transaction with the smart Contract")
            object SIGNING_TRANSACTION: ProgressTracker.Step("Signing the transaction in the iniator Node")
            object GATHERING_SIGNATURE: ProgressTracker.Step("Gathereing the counter party signature"){
                override fun childProgressTracker()= CollectSignaturesFlow.tracker()

            }

            object FINALYZING_TRANSACTION: ProgressTracker.Step("Obtaining notary signature and recording transaction"){
                override fun childProgressTracker()= FinalityFlow.tracker()

            }

            fun tracker()=ProgressTracker(
                    GENERATE_TRANSACTION,
                    VERIFY_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGNATURE,
                    FINALYZING_TRANSACTION
            )

        }
        override val progressTracker= tracker()

        @Suspendable
        override fun call(): SignedTransaction {
            val notary = serviceHub.networkMapCache.notaryIdentities.first()

            progressTracker.currentStep= GENERATE_TRANSACTION
            val complaint= WhistleState(
                    whistle.tipNo,
                    whistle.encryptedSecret,
                    whistle.company,
                    whistle.incidentType,
                    whistle.association,
                    whistle.awareOf,
                    whistle.personInvolved,
                    whistle.monetorValue,
                    whistle.date,
                    whistle.auditAware,
                    whistle.generalNature,
                    whistle.occurancePlace,
                    whistle.reward,
                    whistle.reviewer
            )
            val command= Command(WhistleContract.Commands.ComplaintReg(),complaint.participants.map { it.owningKey })
            val txBuilder=TransactionBuilder(notary)
                    .addOutputState(complaint,ID)
                    .addCommand(command)

            progressTracker.currentStep= VERIFY_TRANSACTION
            txBuilder.verify(serviceHub)

            progressTracker.currentStep= SIGNING_TRANSACTION
            val partiallysignedtx=serviceHub.signInitialTransaction(txBuilder)

            progressTracker.currentStep= GATHERING_SIGNATURE
            val otherPartyFlow=initiateFlow(whistle.reviewer)
            val fullysignedtx=subFlow(CollectSignaturesFlow(partiallysignedtx,setOf(otherPartyFlow), Companion.GATHERING_SIGNATURE.childProgressTracker()))

            progressTracker.currentStep= FINALYZING_TRANSACTION
            return subFlow(FinalityFlow(fullysignedtx, Companion.FINALYZING_TRANSACTION.childProgressTracker()))

        }
    }
    @InitiatedBy(Initiator::class)
    class Acceptor(val otherPartyFlow:FlowSession) :FlowLogic<SignedTransaction>()
    {
        @Suspendable
        override fun call(): SignedTransaction {
            val signedTransactionFlow=object :SignTransactionFlow(otherPartyFlow){
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val data=stx.tx.outputs.single().data
                    val complaint=data as WhistleState
                    "The compalaint monetory value  should not be less than zero" using(complaint.monetorValue!="null")

                }
            }
            return subFlow(signedTransactionFlow)
        }
    }
}

