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
import org.scalatest.concurrent.ScalaFutures
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http._
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.SubscriptionDisplayConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.SubscriptionDisplayResponseDetail

class SubscriptionDisplayConnectorSpec extends IntegrationTestSpec with ScalaFutures {

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      Map(
        "microservice.services.subscription-display.host"                         -> Host,
        "microservice.services.subscription-display.port"                         -> Port,
        "microservice.services.subscription-display.context"                      -> "subscription-display",
        "auditing.enabled"                                                        -> false,
        "auditing.consumer.baseUri.host"                                          -> Host,
        "auditing.consumer.baseUri.port"                                          -> Port
      )
    )
    .build()

  private lazy val connector                  = app.injector.instanceOf[SubscriptionDisplayConnector]
  private val requestEori                     = "GB083456789000"
  private val requestAcknowledgementReference = "1234567890ABCDEFG"

  private val reqEori =
    Seq(("regime", "CDS"), ("EORI", requestEori), ("acknowledgementReference", requestAcknowledgementReference))

  private val expectedResponse = Json
    .parse(SubscriptionDisplay.validResponse(typeOfLegalEntity = "0001","0100086619"))
    .as[SubscriptionDisplayResponseDetail]


  implicit val hc: HeaderCarrier = HeaderCarrier()

  before {
    resetMockServer()
  }

  override def beforeAll: Unit =
    startMockServer()

  override def afterAll: Unit =
    stopMockServer()

  "SubscriptionDisplay SUB09" should {
    "return successful response with OK status when subscription display service returns 200, for EoriNo and journey is Subscription" in {

      SubscriptionDisplay.returnSubscriptionDisplayWhenReceiveRequest(
        requestEori,
        requestAcknowledgementReference
      )
      await(connector.call(reqEori)) mustBe expectedResponse
    }

  }
}
