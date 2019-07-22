package com.fixnix.server

import com.fixnix.flow.ComplaintCreateFlow.Initiator
import com.fixnix.modal.CreateWhistleFlowRequest
import com.fixnix.state.WhistleState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.startTrackedFlow
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

/**
 *  A Spring Boot Server API controller for interacting with the node via RPC.
 */

@RestController
@RequestMapping("/fixnix/") // The paths for GET and POST requests are relative to this base path.
class MainController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val proxy = rpc.proxy

    /**
     * Returns the node's name.
     */
    @GetMapping(value = [ "me" ], produces = [ APPLICATION_JSON_VALUE ])
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @GetMapping(value = [ "peers" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }


    @GetMapping(value = [ "whistles" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getIOUs() : ResponseEntity<List<StateAndRef<WhistleState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<WhistleState>().states)
    }



    @PostMapping(value = [ "create-iou" ], produces = [ TEXT_PLAIN_VALUE ], headers = [ "Content-Type=application/json" ])
    fun createIOU(request: HttpServletRequest): ResponseEntity<String> {


        val company_Name = request.getParameter("company")
        val typeOfIncidient = request.getParameter("incident")
        val association = request.getParameter("association")
        val howdoyouaware = request.getParameter("aware")
        val personsInvolved = request.getParameter("persons")
        val monetaryValue = request.getParameter("fraud")
        val periodofincident=request.getParameter("periodofincident")
        val authoritiesKnow = request.getParameter("authoritiesKnow")
        val nature = request.getParameter("nature")
        val place = request.getParameter("placeofoccurance")
        val rewardType= request.getParameter("reward")
        val reviewer="O=Fixnix,L=London,C=GB"
        val encryptedSecret="2323232323"


        val partyRPS = CordaX500Name.parse(reviewer)

        val companyRPS = CordaX500Name.parse(company_Name)

        val party = proxy.wellKnownPartyFromX500Name(partyRPS) ?: return ResponseEntity.badRequest().body("Party named $reviewer cannot be found.\n")
        val company = proxy.wellKnownPartyFromX500Name(companyRPS) ?: return ResponseEntity.badRequest().body("Party named $company_Name cannot be found.\n")

        if(typeOfIncidient == null){
            return ResponseEntity.badRequest().body("Query parameter 'CompanyName' must not be null.\n")
        }

        val whistle=CreateWhistleFlowRequest(
                UUID.randomUUID().toString(),
                encryptedSecret,
                company,
                typeOfIncidient,
                association,
                howdoyouaware,
                personsInvolved,
                monetaryValue,
                periodofincident,
                authoritiesKnow,
                nature,
                place,
                rewardType,
                party
                )

        return try {
            val signedTx = proxy.startTrackedFlow(::Initiator, whistle).returnValue.getOrThrow()
            ResponseEntity.status(HttpStatus.CREATED).body("Transaction id ${signedTx.id} committed to ledger.\n")

        } catch (ex: Throwable) {
            logger.error(ex.message, ex)
            ResponseEntity.badRequest().body(ex.message!!)
        }
    }

    @GetMapping(value = [ "my-whistles" ], produces = [ APPLICATION_JSON_VALUE ])
    fun getMyIOUs(): ResponseEntity<List<StateAndRef<WhistleState>>>  {
        val myious = proxy.vaultQueryBy<WhistleState>().states.filter { it.state.data.encryptedSecret.equals(proxy.nodeInfo().legalIdentities.first())  }
        return ResponseEntity.ok(myious)
    }


}
