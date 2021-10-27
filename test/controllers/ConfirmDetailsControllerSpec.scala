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
import play.api.inject
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.AuthRedirectSupport
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmDetails._
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  EstablishmentAddress,
  ServiceUnavailableResponse,
  SubscriptionDisplayResponseDetail
}
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import java.time.LocalDate
import scala.concurrent.Future

class ConfirmDetailsControllerSpec extends BaseSpec with AuthRedirectSupport {

  val subscriptionDisplayConnector = mock[SubscriptionDisplayConnector]

  val establishmentAddress = EstablishmentAddress(
    streetAndNumber = "line1",
    city = "City name",
    postalCode = Some("SE28 1AA"),
    countryCode = "GB"
  )

  val subscriptionDisplayResponse = SubscriptionDisplayResponseDetail(
    EORINo = Some("GB123456789012"),
    CDSFullName = "FirstName LastName",
    CDSEstablishmentAddress = establishmentAddress,
    VATIDs = None,
    shortName = Some("Short Name"),
    dateOfEstablishment = Some(LocalDate.now()),
    XIEORINo = Some("XIE9XSDF10BCKEYAX")
  )

  override def application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[SubscriptionDisplayConnector].to(subscriptionDisplayConnector)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  "ConfirmDetails controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(subscriptionDisplayConnector.call(any())(any()))
          .thenReturn(Future.successful(Right(subscriptionDisplayResponse)))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("Confirm details")
      }
    }

    "return a Bad Request and errors when invalid data is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(subscriptionDisplayConnector.call(any())(any()))
          .thenReturn(Future.successful(Right(subscriptionDisplayResponse)))
        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          defaultUserId,
          Map("value" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Tell us if these details are correct")
      }
    }

    "redirect InternalServerError when Subscription Display call fails" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(subscriptionDisplayConnector.call(any())(any()))
          .thenReturn(Future.successful(Left(ServiceUnavailableResponse)))
        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          defaultUserId,
          Map("value" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "redirect to the Consent page when user selects Yes to confirm details" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          defaultUserId,
          Map("value" -> confirmedDetails.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HaveEUEoriController.onPageLoad().url
      }
    }

    "redirect to the Sign in page when user selects No and wants to use different login credentials" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          defaultUserId,
          Map("value" -> changeCredentials.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(result).get should startWith("/bas-gateway/sign-in")
      }
    }

    "redirect to the Change Details page when user selects No for changing the details" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ConfirmDetailsController.submit().url,
          defaultUserId,
          Map("value" -> changeDetails.toString)
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded().url
      }
    }
  }
}
