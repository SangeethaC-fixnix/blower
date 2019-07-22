package com.fixnix.state

import com.fixnix.contract.WhistleContract
import com.fixnix.schema.OrderSchemaV1
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState


@BelongsToContract(WhistleContract::class)
data class WhistleState(

                          val tipNo: String,
                          val encryptedSecret:String,
                          val company:Party,
                          val incidentType:String,
                          val association: String,
                          val awareOf:String,
                          val personInvolved: String,
                          val monetorValue: String,
                          val date: String,
                          val auditAware:String,
                          val generalNature:String,
                          val occurancePlace:String,
                          val reward: String,
                          val reviewer:Party,
                          override val linearId: UniqueIdentifier = UniqueIdentifier()): LinearState, QueryableState {
    override val participants: List<AbstractParty>
        get() = listOf(company,reviewer)
    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when(schema)
        {
            is OrderSchemaV1 -> OrderSchemaV1.PersistantOrder(
                    this.tipNo,
                    this.encryptedSecret,
                    this.company.toString(),
                    this.incidentType,
                    this.association,
                    this.awareOf,
                    this.personInvolved,
                    this.monetorValue,
                    this.date,
                    this.auditAware,
                    this.generalNature,
                    this.occurancePlace,
                    this.reward,
                    this.reviewer.toString(),
                    this.linearId.id
            )
            else-> throw IllegalArgumentException("There is no schemas found")
        }
    }


    override fun supportedSchemas(): Iterable<MappedSchema> {
        return listOf(OrderSchemaV1)

    }
}



