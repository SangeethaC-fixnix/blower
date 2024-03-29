package com.fixnix.schema

import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for WhistleState.
 */
object OrderSchema
object OrderSchemaV1:MappedSchema(
        schemaFamily = OrderSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistantOrder::class.java)
)
{
    @Entity
    @Table( name = "complaintstate")
    class PersistantOrder(
            @Column(name="tipNo")
            var tipNo: String,

            @Column(name="encryptedSecret")
            var encryptedSecret: String,

            @Column(name="company")
            var company: String,

            @Column(name="incidentType")
            var incidentType: String,

            @Column(name="association")
            var association: String,

            @Column(name="awareOf")
            var awareOf: String,

            @Column(name="personInvolved")
            var personInvolved: String,

            @Column(name="monetorValue")
            var monetorValue: String,

            @Column(name="date")
            var date: String,

            @Column(name="auditAware")
            var auditAware: String,

            @Column(name="generalNature")
            var generalNature: String,

            @Column(name="occurancePlace")
            var occurancePlace: String,

            @Column(name="reward")
            var reward: String,

            @Column(name="reviewer")
            var reviewer: String,

            @Column(name="id")
            var id: UUID

    ):PersistentState(){
        constructor(): this("","","","","","","","","","","","","","", UUID.randomUUID())
    }


}