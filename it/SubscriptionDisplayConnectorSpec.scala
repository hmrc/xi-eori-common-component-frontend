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

import externalservices.ExternalServiceConfig.{Host, Port}
import externalservices.SubscriptionDisplay
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.{Application, inject}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.mvc.Http.Status.SERVICE_UNAVAILABLE
import uk.gov.hmrc.http._
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.SessionCache
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{ServiceUnavailableResponse, SubscriptionDisplayResponseDetail}

import scala.concurrent.Future

class SubscriptionDisplayConnectorSpec extends IntegrationTestSpec with ScalaFutures {
 val mockSessionCache = mock[SessionCache]
  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .overrides( inject.bind[SessionCache].to(mockSessionCache))
    .configure(
      Map(
        "microservxi-eori-common-componentice.services.xi-eori-common-component.host"                         -> Host,
        "microservice.services.xi-eori-common-component.port"                         -> Port,
        "microservice.services.xi-eori-common-component.context"                      -> "xi-eori-common-component",
        "auditing.enabled"                                                        -> false,
        "auditing.consumer.baseUri.host"                                          -> Host,
        "auditing.consumer.baseUri.port"                                          -> Port
      )
    )
    .build()

  private lazy val connector                  = app.injector.instanceOf[SubscriptionDisplayConnector]
  private val requestTaxPayerId               = "GBE9XSDF10BCKEYAX"
  private val requestEori                     = "GB083456789000"
  private val requestAcknowledgementReference = "1234567890ABCDEFG"

  private val reqEori =
    Seq(("regime", "CDS"), ("EORI", requestEori), ("acknowledgementReference", requestAcknowledgementReference))

  private val reqTaxPayerId = Seq(
    ("regime", "CDS"),
    ("taxPayerID", requestTaxPayerId),
    ("acknowledgementReference", requestAcknowledgementReference)
  )

  private val expectedResponse = Json
    .parse(SubscriptionDisplay.validResponse(typeOfLegalEntity = "0001","0100086619"))
    .as[SubscriptionDisplayResponseDetail]


  implicit val hc: HeaderCarrier = HeaderCarrier()

  before {
    resetMockServer()
  }

  override def beforeAll: Unit = {
    startMockServer()
    when(mockSessionCache.saveSubscriptionDisplay(any())(any()))
      .thenReturn(Future.successful(true))

  }

  override def afterAll: Unit =
    stopMockServer()

  "SubscriptionDisplay SUB09" should {
    "return successful response with OK status when subscription display service returns 200, for EoriNo and journey is Subscription" in {

      SubscriptionDisplay.returnSubscriptionDisplayWhenReceiveRequest(
        requestEori,
        requestAcknowledgementReference
      )

      await(connector.call(reqEori)) mustBe Right(expectedResponse)
    }


    "return Service Unavailable Response when subscription display service returns an exception" in {

      SubscriptionDisplay.returnSubscriptionDisplayWhenReceiveRequest(
        requestTaxPayerId,
        requestAcknowledgementReference,
        returnedStatus = SERVICE_UNAVAILABLE
      )


      await(connector.call(reqTaxPayerId)) mustBe Left(ServiceUnavailableResponse)
    }

  }
}
