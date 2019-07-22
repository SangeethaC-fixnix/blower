package com.fixnix.modal

import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
data class CreateWhistleFlowRequest (

    val tipNo: String,
    val encryptedSecret:String,
    val company: Party,
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
    val reviewer: Party
    )