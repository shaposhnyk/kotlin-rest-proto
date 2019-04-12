package com.sh.restproto

import com.sh.Protos
import org.hamcrest.CoreMatchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@RunWith(SpringRunner::class)
@WebMvcTest(CustomerRestController::class)
class CustomerRestControllerTest {

    @Autowired
    internal lateinit var mvc: MockMvc

    @MockBean
    internal lateinit var repo: CustomerRepo

    @Test
    fun checkJsonCanBeProduced() {
        val allEmployees = listOf(Protos.Customer.newBuilder()
                .setId(10)
                .setLastName("VSH")
                .build()
        )

        given(repo.findAll()).willReturn(allEmployees)

        mvc.perform(get("/customers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("\$.customer[0].lastName", `is`("VSH")))
    }

    @Test
    fun checkProtobufCanBeProduced() {
        val allEmployees = listOf(Protos.Customer.newBuilder()
                .setId(10)
                .setLastName("VSH")
                .build()
        )

        given(repo.findAll()).willReturn(allEmployees)

        mvc.perform(get("/customers")
                .contentType("application/x-protobuf"))
                .andExpect(status().isOk())
    }
}
