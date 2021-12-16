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
import play.api.test.Helpers._
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.HavePBE
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import scala.concurrent.Future

class HavePBEControllerSpec extends BaseSpec {

  "HavePBE controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getHavePBEInNI()(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HavePBEController.onPageLoad().url
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("Do you have a permanent business establishment in Northern Ireland?")
      }
    }

    "populate View if userAnswersCache has session data" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getHavePBEInNI()(any())).thenReturn(Future.successful(Some(true)))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HavePBEController.onPageLoad().url
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))
        page.getElementValue("//*[@id='value']") shouldBe "yes"
      }
    }

    "redirect to PBE Address Lookup Page when Yes is selected" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.cacheHavePBEInNI(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HavePBEController.submit().url,
          Map("value" -> HavePBE.values.head.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.PBEAddressLookupController.onPageLoad().url
      }

    }

    "redirect to XiEoriNotNeeded Page when No is selected" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HavePBEController.submit().url,
          Map("value" -> HavePBE.values.last.toString)
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
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HavePBEController.submit().url,
          Map("value" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Select your public business establishment preference")
      }
    }

  }
}
