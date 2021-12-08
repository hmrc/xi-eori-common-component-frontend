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
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmDetails._
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.SubscriptionDisplayService
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder
import util.{BaseSpec, SpecData}

import scala.concurrent.Future

class ConfirmDetailsControllerSpec extends BaseSpec with SpecData {

  val subscriptionDisplayService: SubscriptionDisplayService = mock[SubscriptionDisplayService]
  val mockGroupEnrolmentExtractor: GroupEnrolmentExtractor   = mock[GroupEnrolmentExtractor]

  override def application: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[UserAnswersCache].to(mockUserAnswersCache),
    inject.bind[SessionCache].to(mockSessionCache)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  val registerLinkXpath: String = "//*[@id='vat-register-link']"
  "ConfirmDetails controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(Some(subscriptionDisplayResponse))
        )
        when(mockUserAnswersCache.getConfirmDetails()(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.onPageLoad().url
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("Confirm details")
      }
    }

    "populate View if userAnswersCache has session data" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getConfirmDetails()(any())).thenReturn(Future.successful(Some("changeDetails")))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.onPageLoad().url
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))
        page.getElementValue("//*[@id='value-3']") shouldBe "changeDetails"
      }
    }

    "redirect signout page when session cache doesn't hold subscription display details" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.onPageLoad().url
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER

        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage().url
      }
    }

    "redirect sign out page when session cache doesn't hold subscription display details during submit" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          Map("value" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER

        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage().url
      }
    }
    "return a Bad Request and errors when invalid data is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(Some(subscriptionDisplayResponse))
        )
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          Map("value" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Tell us if these details are correct")
      }
    }

    "redirect to the Disclose Personal Details Consent page when user selects Yes to confirm details" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.cacheConfirmDetails(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          Map("value" -> confirmedDetails.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.DisclosePersonalDetailsController.onPageLoad().url
      }
    }

    "redirect to the XiVatRegister page when user clicks on XI Vat register link" in {
      val subscriptionDisplayResponseWithoutXIVatId =
        subscriptionDisplayResponse.copy(XI_Subscription = Some(xiSubscription.copy(XI_VATNumber = None)))

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(Some(subscriptionDisplayResponseWithoutXIVatId))
        )
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.onPageLoad().url
        )

        val result = route(application, request).get
        val page   = RegistrationPage(contentAsString(result))
        page.getElementsHref(
          registerLinkXpath
        ) shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiVatRegisterController.onPageLoad().url
      }
    }

    "redirect to the Sign In page when user selects No and wants to use different login credentials" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.cacheConfirmDetails(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          Map("value" -> changeCredentials.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get should startWith("/bas-gateway/sign-in")
      }
    }

    "redirect to the HaveEUEori page when user selects No for changing the details" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.cacheConfirmDetails(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          Map("value" -> changeDetails.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ChangeDetailsController.incorrectDetails().url
      }
    }
  }
}
