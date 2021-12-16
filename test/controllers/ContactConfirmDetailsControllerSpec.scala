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
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmAddress.{
  changeAddress,
  confirmedAddress,
  enterManually
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import scala.concurrent.Future

class ContactConfirmDetailsControllerSpec extends BaseSpec {

  val address: AddressViewModel = AddressViewModel("line1", "city", Some("postcode"), "GB", None, None)
  "ContactConfirmDetails controller" should {

    "calling onPageLoad " should {
      "return OK and the correct view for a GET" in {

        running(application) {
          withAuthorisedUser(defaultUserId, mockAuthConnector)
          when(mockUserAnswersCache.getContactAddressDetails()(any())).thenReturn(Future.successful(Some(address)))
          when(mockUserAnswersCache.getConfirmContactAddress()(any())).thenReturn(Future.successful(None))
          val request = SessionBuilder.buildRequestWithSessionAndPath(
            uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactConfirmAddressController.onPageLoad().url,
            defaultUserId
          )

          val result = route(application, request).get

          val page = RegistrationPage(contentAsString(result))

          page.title should startWith("Is this your XI EORI application contact address?")
        }
      }
      "populate View if userAnswersCache has session data" in {
        running(application) {
          withAuthorisedUser(defaultUserId, mockAuthConnector)
          when(mockUserAnswersCache.getContactAddressDetails()(any())).thenReturn(Future.successful(Some(address)))
          when(mockUserAnswersCache.getConfirmContactAddress()(any())).thenReturn(
            Future.successful(Some("changeAddress"))
          )
          val request = SessionBuilder.buildRequestWithSessionAndPath(
            uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactConfirmAddressController.onPageLoad().url,
            defaultUserId
          )

          val result = route(application, request).get

          val page = RegistrationPage(contentAsString(result))
          page.getElementValue("//*[@id='value-2']") shouldBe "changeAddress"
        }
      }
      "redirect Sign out page when Session Cache doesn't hold address details" in {
        running(application) {
          withAuthorisedUser(defaultUserId, mockAuthConnector)
          when(mockUserAnswersCache.getContactAddressDetails()(any())).thenReturn(Future.successful(None))
          val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
            uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactConfirmAddressController.onPageLoad().url,
            Map("value" -> "")
          )

          val result = route(application, request).get
          status(result) shouldBe SEE_OTHER

          redirectLocation(
            result
          ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage().url
        }
      }
    }

    "calling onSubmit" should {
      "return a Bad Request and errors when invalid data is submitted" in {
        running(application) {
          withAuthorisedUser(defaultUserId, mockAuthConnector)
          when(mockUserAnswersCache.getContactAddressDetails()(any())).thenReturn(Future.successful(Some(address)))
          val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
            uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactConfirmAddressController.submit().url,
            Map("value" -> "")
          )

          val result = route(application, request).get
          status(result) shouldBe BAD_REQUEST

          val page = RegistrationPage(contentAsString(result))
          page.errors should startWith("Confirm your address details")
        }
      }
      "redirect to the Upload Contact page when user selects Yes to confirm details" in {
        running(application) {
          withAuthorisedUser(defaultUserId, mockAuthConnector)
          when(mockUserAnswersCache.getContactAddressDetails()(any())).thenReturn(Future.successful(Some(address)))
          when(mockUserAnswersCache.cacheContactConfirmAddress(any())(any())).thenReturn(Future.successful(true))
          val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
            uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactConfirmAddressController.submit().url,
            Map("value" -> confirmedAddress.toString)
          )

          val result = route(application, request).get
          status(result) shouldBe SEE_OTHER
          redirectLocation(
            result
          ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmContactDetailsController.onPageLoad().url
        }
      }
      "redirect to the Contact Address Lookup when user selects No and wants to change address" in {
        running(application) {
          withAuthorisedUser(defaultUserId, mockAuthConnector)
          when(mockUserAnswersCache.getContactAddressDetails()(any())).thenReturn(Future.successful(Some(address)))
          when(mockUserAnswersCache.cacheContactConfirmAddress(any())(any())).thenReturn(Future.successful(true))
          val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
            uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactConfirmAddressController.submit().url,
            Map("value" -> changeAddress.toString)
          )

          val result = route(application, request).get
          status(result) shouldBe SEE_OTHER
          redirectLocation(
            result
          ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactAddressLookupController.onPageLoad().url
        }
      }
      "redirect to the Enter Manually page when user selects No and wants to enter address manually" in {
        running(application) {
          withAuthorisedUser(defaultUserId, mockAuthConnector)
          when(mockUserAnswersCache.getContactAddressDetails()(any())).thenReturn(Future.successful(Some(address)))
          when(mockUserAnswersCache.cacheContactConfirmAddress(any())(any())).thenReturn(Future.successful(true))
          val request = SessionBuilder.buildPostRequestWithSessionAndPathAndFormValues(
            uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactConfirmAddressController.submit().url,
            Map("value" -> enterManually.toString)
          )

          val result = route(application, request).get
          status(result) shouldBe SEE_OTHER
          redirectLocation(
            result
          ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualContactAddressController.onPageLoad().url
        }
      }
    }

  }
}
