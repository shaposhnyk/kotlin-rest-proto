package com.sh.restproto

import com.sh.Protos
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
internal class CustomerRestController {
    @Autowired
    internal lateinit var courseRepo: CustomerRepo

    /** `curl -v http://localhost:8080/customers/1`  */
    @GetMapping(value = arrayOf("/customers/{id}"), produces = arrayOf("application/json; charset=UTF-8", "application/x-protobuf; messageType=com.sh.proto.dtos.Customer"))
    fun customer(@PathVariable id: Int): Protos.CustomerResult {
        try {
            return Protos.CustomerResult.newBuilder().setResult(courseRepo.findByRef(id)).build()
        } catch (e: IndexOutOfBoundsException) {
            return Protos.CustomerResult.newBuilder().setError(
                    Protos.Error.newBuilder()
                            .setMessage("Item not found")
                            .setCode("NF/404")
            ).build()
        }
    }

    /** `curl -v http://localhost:8080/customers`  */
    @GetMapping(value = arrayOf("/customers"), produces = arrayOf("application/json; charset=UTF-8", "application/x-protobuf; messageType=com.sh.proto.dtos.Customer"))
    fun customers(): Protos.CustomerList {
        return Protos.CustomerList.newBuilder().addAllCustomer(courseRepo.findAll()).build()
    }
}
