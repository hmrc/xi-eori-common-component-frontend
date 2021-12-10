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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{inject, Application}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.GroupEnrolmentExtractor
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.SubscriptionDisplayService
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder
import util.{BaseSpec, SpecData}
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes._

import scala.concurrent.Future

class ConfirmContactDetailsControllerSpec extends BaseSpec with SpecData {

  val subscriptionDisplayService: SubscriptionDisplayService = mock[SubscriptionDisplayService]
  val mockGroupEnrolmentExtractor: GroupEnrolmentExtractor   = mock[GroupEnrolmentExtractor]

  override def application: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[UserAnswersCache].to(mockUserAnswersCache),
    inject.bind[SessionCache].to(mockSessionCache)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  val registerLinkXpath: String = "//*[@id='vat-register-link']"
  "ConfirmContactDetails controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(Some(subscriptionDisplayResponse))
        )
        val request = SessionBuilder.buildRequestWithSessionAndPath(ConfirmContactDetailsController.onPageLoad().url)

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("XI EORI application contact details")
      }
    }

    "display error page when session cache holds subscription display details but no contact information" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(Some(subscriptionDisplayResponse.copy(contactInformation = None)))
        )
        val request = SessionBuilder.buildRequestWithSessionAndPath(ConfirmContactDetailsController.onPageLoad().url)

        val result = route(application, request).get

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "display error page when session cache holds subscription display details but contact information has missing field" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(
            Some(
              subscriptionDisplayResponse.copy(contactInformation =
                Some(contactInformation.copy(personOfContact = None))
              )
            )
          )
        )
        val request = SessionBuilder.buildRequestWithSessionAndPath(ConfirmContactDetailsController.onPageLoad().url)

        val result = route(application, request).get

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to signout page when session cache doesn't hold subscription display details" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(ConfirmContactDetailsController.onPageLoad().url)

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER

        redirectLocation(result).get shouldBe LogoutController.displayTimeOutPage().url
      }
    }
  }
}
