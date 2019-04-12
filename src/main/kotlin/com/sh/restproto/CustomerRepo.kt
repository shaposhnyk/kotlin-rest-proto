package com.sh.restproto

import com.sh.Protos

open class CustomerRepo {

    fun findByRef(id: Int): Protos.Customer {
        return CUSTOMERS[id]
    }

    open fun findAll(): List<Protos.Customer> {
        return CUSTOMERS
    }

    companion object {
        private val CUSTOMERS = IntRange(1000000, 1001000)
                .map { ref ->
                    Protos.Customer.newBuilder()
                            .setLastName("VSH")
                            .setId(ref)
                            .setFirstName("ABC/R$ref")
                            .setLegalEntityCode("ABC")
                            .setLegalEntityCode1("ABC")
                            .setLegalEntityCode2("ABC")
                            .setLegalEntityCode3("ABC")
                            .setLegalEntityCode4("ABC")
                            .setLegalEntityCode5("ABC")
                            .setLegalEntityCode6("ABC")
                            .setLegalEntityCode7("ABC")
                            .setLegalEntityCode8("ABC")
                            .setLegalEntityCode9("ABC")
                            .setLegalEntityCodeA("ABC")
                            .setLegalEntityCodeB("ABC")
                            .setLegalEntityCodeC("ABC")
                            .build()
                }
    }
}