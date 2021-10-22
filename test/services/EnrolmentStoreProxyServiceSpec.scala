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
import org.scalatest.BeforeAndAfter
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.{await, defaultAwaitTimeout, running}
import play.api.{inject, Application}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.connectors.EnrolmentStoreProxyConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain._
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.EnrolmentStoreProxyService
import util.BaseSpec

import scala.concurrent.Future

class EnrolmentStoreProxyServiceSpec extends BaseSpec with BeforeAndAfter {

  private val mockEnrolmentStoreProxyConnector =
    mock[EnrolmentStoreProxyConnector]

  override val application: Application =
    new GuiceApplicationBuilder().overrides(
      inject.bind[EnrolmentStoreProxyConnector].toInstance(mockEnrolmentStoreProxyConnector)
    ).configure("metrics.jvm" -> false, "metrics.enabled" -> false)
      .build()

  private val service                               = application.injector.instanceOf[EnrolmentStoreProxyService]
  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  before {
    reset(mockEnrolmentStoreProxyConnector)
  }

  private val serviceName = "HMRC-CUS-ORG"
  private val state       = "Activated"
  private val identifier  = KeyValue("EORINumber", "10000000000000001")
  private val groupId     = GroupId("groupId")

  private val enrolmentResponse =
    EnrolmentResponse(serviceName, state, List(identifier))

  private val enrolmentResponseNotActive =
    EnrolmentResponse("SOME_SERVICE", "NotActive", List(identifier))

  private val serviceName1 = "HMRC-VAT-ORG"

  private val enrolmentResponseNoHmrcCusOrg =
    EnrolmentResponse(serviceName1, state, List(identifier))

  "EnrolmentStoreProxyService" should {

    "return all enrolments for the groupId" in {
      when(
        mockEnrolmentStoreProxyConnector
          .getEnrolmentByGroupId(any[String])(meq(headerCarrier), any())
      ).thenReturn(
        Future.successful(EnrolmentStoreProxyResponse(List(enrolmentResponse, enrolmentResponseNoHmrcCusOrg)))
      )
      running(application) {
        await(service.enrolmentsForGroup(groupId)) shouldBe List(enrolmentResponse, enrolmentResponseNoHmrcCusOrg)

        verify(mockEnrolmentStoreProxyConnector).getEnrolmentByGroupId(any[String])(meq(headerCarrier), any())

      }
    }

    "exclude non-active enrolments for the groupId" in {
      when(
        mockEnrolmentStoreProxyConnector
          .getEnrolmentByGroupId(any[String])(meq(headerCarrier), any())
      ).thenReturn(Future.successful(EnrolmentStoreProxyResponse(List(enrolmentResponse, enrolmentResponseNotActive))))
      running(application) {

        await(service.enrolmentsForGroup(groupId)) shouldBe List(enrolmentResponse)

        verify(mockEnrolmentStoreProxyConnector).getEnrolmentByGroupId(any[String])(meq(headerCarrier), any())
      }
    }
  }
}
