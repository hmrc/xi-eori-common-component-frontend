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
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.SubscriptionDisplayService
import util.SpecData

import scala.concurrent.Future

class SubscriptionDisplayServiceSpec
    extends WordSpec with BeforeAndAfter with MockitoSugar with Matchers with SpecData {

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

  private val eori = Eori("GB123456789012")

  "SubscriptionDisplayService" should {

    "return subscription details for the gbEori" in {
      when(
        mockSubscriptionDisplayConnector
          .call(any())(meq(headerCarrier))
      ).thenReturn(Future.successful(Right(subscriptionDisplayResponse)))
      when(
        mockSessionCache
          .subscriptionDisplay(meq(headerCarrier))
      ).thenReturn(Future.successful(None))
      when(
        mockSessionCache
          .saveSubscriptionDisplay(any())(any())
      ).thenReturn(Future.successful(true))

      running(application) {
        await(service.getSubscriptionDisplay(eori.id)) shouldBe Right(subscriptionDisplayResponse)

        verify(mockSubscriptionDisplayConnector).call(any())(meq(headerCarrier))

      }
    }

    "return subscription details from the sessionCache" in {
      when(
        mockSubscriptionDisplayConnector
          .call(any())(meq(headerCarrier))
      ).thenReturn(Future.successful(Right(subscriptionDisplayResponse)))
      when(
        mockSessionCache
          .subscriptionDisplay(meq(headerCarrier))
      ).thenReturn(Future.successful(Some(subscriptionDisplayResponse)))

      running(application) {
        await(service.getSubscriptionDisplay(eori.id)) shouldBe Right(subscriptionDisplayResponse)

        verify(mockSubscriptionDisplayConnector, never()).call(any())(meq(headerCarrier))

      }
    }

  }
}
