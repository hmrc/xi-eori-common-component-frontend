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

package services

import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito._
import org.scalatest.{BeforeAndAfter, Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, defaultAwaitTimeout, running}
import play.api.{inject, Application}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain._
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  EstablishmentAddress,
  SubscriptionDisplayResponseDetail,
  SubscriptionInfoVatId
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.SubscriptionDisplayService

import java.time.LocalDate
import scala.concurrent.Future

class SubscriptionDisplayServiceSpec extends WordSpec with BeforeAndAfter with MockitoSugar with Matchers {

  private val mockSubscriptionDisplayConnector =
    mock[SubscriptionDisplayConnector]

  private val mockSessionCache = mock[SessionCache]

  val application: Application =
    new GuiceApplicationBuilder().overrides(
      inject.bind[SubscriptionDisplayConnector].toInstance(mockSubscriptionDisplayConnector),
      inject.bind[SessionCache].toInstance(mockSessionCache)
    ).configure("metrics.jvm" -> false, "metrics.enabled" -> false)
      .build()

  private val service                               = application.injector.instanceOf[SubscriptionDisplayService]
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  before {
    reset(mockSubscriptionDisplayConnector)
  }

  val subscriptionResponse = SubscriptionDisplayResponseDetail(
    Some("EN123456789012345"),
    "John Doe",
    EstablishmentAddress("house no Line 1", "city name", Some("SE28 1AA"), "ZZ"),
    Some(List(SubscriptionInfoVatId(Some("GB"), Some("999999")), SubscriptionInfoVatId(Some("ES"), Some("888888")))),
    Some("Doe"),
    Some(LocalDate.of(1963, 4, 1)),
    Some("XIE9XSDF10BCKEYAX")
  )

  private val eori = Eori("GB123456789012")

  "SubscriptionDisplayService" should {

    "return subscription details for the gbEori" in {
      when(
        mockSubscriptionDisplayConnector
          .call(any())(meq(headerCarrier))
      ).thenReturn(Future.successful(Right(subscriptionResponse)))
      when(
        mockSessionCache
          .subscriptionDisplay(meq(headerCarrier))
      ).thenReturn(Future.successful(None))

      running(application) {
        await(service.getSubscriptionDisplay(eori.id)) shouldBe Right(subscriptionResponse)

        verify(mockSubscriptionDisplayConnector).call(any())(meq(headerCarrier))

      }
    }

    "return subscription details from the sessionCache" in {
      when(
        mockSubscriptionDisplayConnector
          .call(any())(meq(headerCarrier))
      ).thenReturn(Future.successful(Right(subscriptionResponse)))
      when(
        mockSessionCache
          .subscriptionDisplay(meq(headerCarrier))
      ).thenReturn(Future.successful(Some(subscriptionResponse)))

      running(application) {
        await(service.getSubscriptionDisplay(eori.id)) shouldBe Right(subscriptionResponse)

        verify(mockSubscriptionDisplayConnector, never()).call(any())(meq(headerCarrier))

      }
    }

    /*  "exclude non-active enrolments for the groupId" in {
      when(
        mockEnrolmentStoreProxyConnector
          .getEnrolmentByGroupId(any[String])(meq(headerCarrier), any())
      ).thenReturn(Future.successful(EnrolmentStoreProxyResponse(List(enrolmentResponse, enrolmentResponseNotActive))))
      running(application) {

        await(service.enrolmentsForGroup(groupId)) shouldBe List(enrolmentResponse)

        verify(mockEnrolmentStoreProxyConnector).getEnrolmentByGroupId(any[String])(meq(headerCarrier), any())
      }
    }*/
  }
}
