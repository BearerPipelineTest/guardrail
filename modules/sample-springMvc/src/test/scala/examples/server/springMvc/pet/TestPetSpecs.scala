package examples.server.springMvc.pet

import java.util
import java.util.concurrent.CompletableFuture

import examples.server.springMvc.definitions.Pet
import examples.server.springMvc.pet.PetHandler.FindPetsByStatusResponse
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import org.mockito.{ ArgumentMatchersSugar, MockitoSugar }
import org.scalatest.{ BeforeAndAfterAll, FreeSpec, Matchers }
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.{ Bean, ComponentScan }
import org.springframework.http.MediaType
import org.springframework.test.context.TestContextManager
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.{ asyncDispatch, get, post }
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{ request, status }

@RunWith(classOf[SpringRunner])
@SpringBootTest(classes = Array(classOf[TestApplication]))
@AutoConfigureMockMvc
@ComponentScan
@EnableAutoConfiguration
class TestPetSpecs extends FreeSpec with Matchers with BeforeAndAfterAll with MockitoSugar with ArgumentMatchersSugar {
  @Autowired var mvc: MockMvc               = _
  @Autowired var petHandlerMock: PetHandler = _

  new TestContextManager(this.getClass).prepareTestInstance(this)

  override def beforeAll(): Unit =
    reset(petHandlerMock)

  "let's test pet-store generation" - {
    "should be able to execute simple get" in {
      val entityBody = new util.ArrayList[Pet]()
      entityBody.add(new Pet.Builder("cat").build())
      entityBody.add(new Pet.Builder("mouse").build())

      when(petHandlerMock.findPetsByStatus(isA[java.util.List[String]]))
        .thenReturn(CompletableFuture.completedFuture(FindPetsByStatusResponse.Ok(entityBody)))

      val mvcResult = mvc
        .perform(
          get("/v2/pet/findByStatus?status=1")
            .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(request.asyncStarted)
        .andReturn

      mvc.perform(asyncDispatch(mvcResult)).andDo(print()).andExpect(status.isOk)

      val content = mvcResult.getResponse.getContentAsString
      assertTrue(content.contains("cat"))
      assertTrue(content.contains("mouse"))
    }

    "update pet with form" in {

      when(petHandlerMock.updatePetWithForm(1L, java.util.Optional.of("Blah"), java.util.Optional.of("some_status")))
        .thenReturn(CompletableFuture.completedFuture(PetHandler.UpdatePetWithFormResponse.BadRequest))

      val mvcResult = mvc
        .perform(
          post("/v2/pet/1")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .content("name=Blah&status=some_status")
        )
        .andExpect(request.asyncStarted)
        .andReturn

      mvcResult.getRequest.getPathInfo shouldBe ("/v2/pet/1")

      mvc
        .perform(asyncDispatch(mvcResult))
        .andDo(print())
        .andExpect(status.isBadRequest)
    }

  }
}
