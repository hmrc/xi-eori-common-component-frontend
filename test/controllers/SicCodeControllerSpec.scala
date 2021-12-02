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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.{AffinityGroup, AuthConnector}
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.GroupEnrolmentExtractor
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.{EnrolmentResponse, KeyValue}
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  EstablishmentAddress,
  ServiceUnavailableResponse,
  SubscriptionDisplayResponseDetail,
  SubscriptionInfoVatId,
  XiSubscription
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.SubscriptionDisplayService
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import java.time.LocalDate
import scala.concurrent.Future

class SicCodeControllerSpec extends BaseSpec {

  val subscriptionDisplayService: SubscriptionDisplayService = mock[SubscriptionDisplayService]
  val mockGroupEnrolmentExtractor: GroupEnrolmentExtractor   = mock[GroupEnrolmentExtractor]

  val nonNIEstablishmentAddress: EstablishmentAddress = EstablishmentAddress(
    streetAndNumber = "line1",
    city = "City name",
    postalCode = Some("SE28 1AA"),
    countryCode = "GB"
  )

  val xiSubscription: XiSubscription = XiSubscription("XI8989989797", Some("7978"))

  val nonNIsubscriptionDisplayResponse: SubscriptionDisplayResponseDetail = SubscriptionDisplayResponseDetail(
    EORINo = Some("GB123456789012"),
    CDSFullName = "FirstName LastName",
    CDSEstablishmentAddress = nonNIEstablishmentAddress,
    VATIDs = None,
    shortName = Some("Short Name"),
    dateOfEstablishment = Some(LocalDate.now()),
    XI_Subscription = Some(xiSubscription)
  )

  val niEstablishmentAddress: EstablishmentAddress = EstablishmentAddress(
    streetAndNumber = "line1",
    city = "City name",
    postalCode = Some("BT28 1AA"),
    countryCode = "GB"
  )

  val niSubscriptionDisplayResponse: SubscriptionDisplayResponseDetail = SubscriptionDisplayResponseDetail(
    EORINo = Some("GB123456789012"),
    CDSFullName = "FirstName LastName",
    CDSEstablishmentAddress = niEstablishmentAddress,
    VATIDs = None,
    shortName = Some("Short Name"),
    dateOfEstablishment = Some(LocalDate.now()),
    XI_Subscription = Some(xiSubscription)
  )

  val groupEnrolment =
    List(EnrolmentResponse("HMRC-ATAR-ORG", "Activated", List(KeyValue("EORINumber", "GB123456463324"))))

  val existingEori: Option[String] = Some("XIE9XSDF10BCKEYAX")

  override def application: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[UserAnswersCache].to(mockUserAnswersCache),
    inject.bind[SessionCache].to(mockSessionCache)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  "SicCode controller" should {
    "return OK and the correct view for a GET" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getSicCode()(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.SicCodeController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("What is your Standard Industrial Classification (SIC) code?")
      }
    }
    "populate View if userAnswersCache has session data" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockUserAnswersCache.getSicCode()(any())).thenReturn(Future.successful(Some("99987")))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.SicCodeController.onPageLoad().url,
          defaultUserId
        )

        val result = route(application, request).get

        val page = RegistrationPage(contentAsString(result))
        page.getElementValue("//*[@id='sic']") shouldBe "99987"
      }
    }
    "redirect to the next page when Organisation group with non NI postcode data is submitted" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Organisation)

        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(Some(nonNIsubscriptionDisplayResponse))
        )
        when(mockUserAnswersCache.cacheSicCode(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.SicCodeController.submit().url,
          defaultUserId,
          Map("sic" -> "12345")
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HavePBEController.onPageLoad().url
      }

    }

    "redirect to the next page when Organisation group with NI postcode data is submitted" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Organisation)
        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(
          Future.successful(Some(niSubscriptionDisplayResponse))
        )
        when(mockUserAnswersCache.cacheSicCode(any())(any())).thenReturn(Future.successful(true))
        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.SicCodeController.submit().url,
          defaultUserId,
          Map("sic" -> "12345")
        )

        val result = route(application, request).get
        status(result) shouldBe SEE_OTHER
        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded().url
      }

    }

    "redirect to the next page when Affinity group is individual" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Individual)

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.SicCodeController.submit().url,
          defaultUserId,
          Map("sic" -> "12345")
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

        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.SicCodeController.submit().url,
          defaultUserId,
          Map("sic" -> "")
        )

        val result = route(application, request).get
        status(result) shouldBe BAD_REQUEST

        val page = RegistrationPage(contentAsString(result))
        page.errors() shouldEqual "Enter a SIC code"
      }
    }

    "redirect to Error Page if session cache doesn't have subscriptoin display response" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector, userAffinityGroup = AffinityGroup.Organisation)

        when(mockSessionCache.subscriptionDisplay(any())).thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPathAndFormValues(
          "POST",
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.SicCodeController.submit().url,
          defaultUserId,
          Map("sic" -> "12345")
        )

        val result = route(application, request).get

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }

  }
}
