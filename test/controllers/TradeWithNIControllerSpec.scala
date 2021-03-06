/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers

import common.pages.RegistrationPage
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers._
import reactivemongo.api.ReadPreference
import uk.gov.hmrc.cache.model.{Cache, Id}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.TradeWithNI
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import scala.concurrent.{ExecutionContext, Future}

class TradeWithNIControllerSpec extends BaseSpec {

  val json: JsValue =
    Json.parse("""
                 |{
                 |  "data": {
                 |    "userAnswers": {
                 |
                 |    }
                 |  }
                 |}""".stripMargin)

  val data: Cache = Cache(mock[Id], Some(json))
  "TradeWithNI controller" should {
    "return OK and the correct view" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        when(mockSessionCache.findById(any(), any[ReadPreference])(any[ExecutionContext])).thenReturn(
          Future.successful(Some(data))
        )
        when(mockUserAnswersCache.getTradeWithInNI()(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.onPageLoad().url
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("Do you move goods in or out of Northern Ireland")
      }
    }

    "display page if GB Eori is not linked for the logged user" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.findById(any(), any[ReadPreference])(any[ExecutionContext])).thenReturn(
          Future.successful(Some(data))
        )
        when(mockUserAnswersCache.getTradeWithInNI()(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.onPageLoad().url
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("Do you move goods in or out of Northern Ireland")
      }
    }

    "populate View if userAnswersCache has session data" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getTradeWithInNI()(any())).thenReturn(Future.successful(Some(true)))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.onPageLoad().url
        )

        val result = route(application, request).get
        status(result) shouldBe OK
        val page = RegistrationPage(contentAsString(result))
        page.getElementValue("//*[@id='value']") shouldBe "yes"
      }
    }
    "redirect to HaveEuEori Page when Yes is selected" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.findById(any(), any[ReadPreference])(any[ExecutionContext])).thenReturn(
          Future.successful(Some(data))
        )
        when(mockUserAnswersCache.cacheTradeWithNI(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.submit().url,
          Map("value" -> TradeWithNI.values.head.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.onPageLoad().url
      }

    }

    "redirect to XiEoriNotNeeded Page when No is selected" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.cacheTradeWithNI(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.submit().url,
          Map("value" -> TradeWithNI.values.last.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded().url
      }

    }

    "return a Bad Request and errors when invalid data is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.submit().url,
          Map("value" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Tell us if you move goods in or out of Northern Ireland")
      }
    }

  }
}
