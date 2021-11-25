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
import play.api.{inject, Application}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector}
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.GroupEnrolmentExtractor
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, KeyValue}
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.UserAnswersCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.SubscriptionDisplayService
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel

import scala.concurrent.Future

class ManualPBEAddressControllerSpec extends BaseSpec {

  val mockGroupEnrolmentExtractor: GroupEnrolmentExtractor   = mock[GroupEnrolmentExtractor]
  val subscriptionDisplayService: SubscriptionDisplayService = mock[SubscriptionDisplayService]

  private def groupEnrolment() =
    List(EnrolmentResponse("HMRC-ATAR-ORG", "Activated", List(KeyValue("EORINumber", "GB123456463324"))))

  override def application: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[UserAnswersCache].to(mockUserAnswersCache),
    inject.bind[GroupEnrolmentExtractor].to(mockGroupEnrolmentExtractor)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  val existingEori: Some[String] = Some("XIE9XSDF10BCKEYAX")

  "ManualPBEAddress controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getAddressDetails()(any())).thenReturn(Future.successful(None))
        when(mockGroupEnrolmentExtractor.groupIdEnrolments(any())(any()))
          .thenReturn(Future.successful(groupEnrolment))

        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("What is your permanent business establishment address?")
      }
    }

    "populate View if userAnswersCache has session data" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getAddressDetails()(any())).thenReturn(
          Future.successful(Some(AddressViewModel("line1", "town", Some("BT11AA"), "GB")))
        )
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))
        page.getElementValue("//*[@id='line1']") shouldBe "line1"
      }
    }

    "return a Bad Request and errors when no line1 is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.submit().url,
          defaultUserId,
          Map("line1" -> "", "townorcity" -> "test", "postcode" -> "BT11AA")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Enter Address line 1")

      }
    }

    "return a Bad Request and errors when no town is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.submit().url,
          defaultUserId,
          Map("line1" -> "Abc", "townorcity" -> "", "postcode" -> "BT11AA")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Enter town or city")
      }
    }

    "return a Bad Request and errors when town lemgth is exceeded" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.submit().url,
          defaultUserId,
          Map("line1" -> "Abc", "townorcity" -> "TEST Town Name greater than 35 chars", "postcode" -> "BT11AA")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should startWith("Town or city must be 35 characters or less")
      }
    }

    "return a Bad Request and errors when no postcode is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.submit().url,
          defaultUserId,
          Map("line1" -> "Abc", "townorcity" -> "test", "postcode" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should endWith("Enter postcode")
      }
    }

    "return a Bad Request and errors when invalid postcode is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.submit().url,
          defaultUserId,
          Map("line1" -> "Abc", "townorcity" -> "test", "postcode" -> "TESTPOST")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should endWith("Enter a valid postcode")
      }
    }

    "return a Bad Request and errors when non BT postcode is submitted" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.submit().url,
          defaultUserId,
          Map("line1" -> "Abc", "townorcity" -> "test", "postcode" -> "PR11UN")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors should endWith("Postcode must start with BT")
      }
    }

    "redirect to the next page when Affinity group is Organisation" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Organisation)
        when(mockGroupEnrolmentExtractor.groupIdEnrolments(any())(any()))
          .thenReturn(Future.successful(groupEnrolment))
        when(mockUserAnswersCache.cacheAddressDetails(any())(any())).thenReturn(Future.successful(true))

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.submit().url,
          defaultUserId,
          Map("line1" -> "Abc", "townorcity" -> "test", "postcode" -> "BT11AA", "country" -> "GB")
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.PBEConfirmAddressController.onPageLoad().url

      }
    }

    "redirect to the next page when Affinity group is Individual" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Individual)
        when(mockUserAnswersCache.getAddressDetails()(any())).thenReturn(Future.successful(None))
        when(mockGroupEnrolmentExtractor.groupIdEnrolments(any())(any()))
          .thenReturn(Future.successful(groupEnrolment))

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.submit().url,
          defaultUserId,
          Map("line1" -> "Abc", "townorcity" -> "test", "postcode" -> "BT11AA", "country" -> "GB")
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded().url

      }
    }

    "return OK and the correct view for review" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ManualPBEAddressController.reviewPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("What is your permanent business establishment address?")
      }
    }
  }
}
