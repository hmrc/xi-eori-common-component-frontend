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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import play.api.{inject, Application}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.GroupEnrolmentExtractor
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.ServiceUnavailableResponse
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.SubscriptionDisplayService
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder
import util.{BaseSpec, SpecData}

import scala.concurrent.Future

class ApplicationControllerSpec extends BaseSpec with SpecData {

  val subscriptionDisplayService: SubscriptionDisplayService = mock[SubscriptionDisplayService]
  val mockGroupEnrolmentExtractor: GroupEnrolmentExtractor   = mock[GroupEnrolmentExtractor]

  override def application: Application = new GuiceApplicationBuilder().overrides(
    inject.bind[AuthConnector].to(mockAuthConnector),
    inject.bind[SubscriptionDisplayService].to(subscriptionDisplayService),
    inject.bind[UserAnswersCache].to(mockUserAnswersCache),
    inject.bind[SessionCache].to(mockSessionCache),
    inject.bind[GroupEnrolmentExtractor].to(mockGroupEnrolmentExtractor)
  ).configure("auditing.enabled" -> "false", "metrics.jvm" -> false, "metrics.enabled" -> false).build()

  "Application controller" should {
    "redirect to trade with NI page if the logged user has no Linked GB Eori" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(mockSessionCache.saveUserAnswers(any())(any()))
          .thenReturn(Future.successful(true))
        when(mockGroupEnrolmentExtractor.getEori(any())(any()))
          .thenReturn(Future.successful(None))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ApplicationController.onPageLoad().url
        )

        val result = route(application, request).get

        status(result) shouldBe SEE_OTHER

        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.onPageLoad().url
      }
    }

    "redirect to XI Already exists page if the logged user has XI EORI already" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(subscriptionDisplayService.getSubscriptionDisplay(any())(any()))
          .thenReturn(Future.successful(Right(subscriptionDisplayResponse)))
        when(mockGroupEnrolmentExtractor.getEori(any())(any()))
          .thenReturn(Future.successful(existingEori))
        when(mockSessionCache.saveUserAnswers(any())(any()))
          .thenReturn(Future.successful(true))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ApplicationController.onPageLoad().url
        )

        val result = route(application, request).get

        status(result) shouldBe SEE_OTHER

        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.AlreadyHaveXIEoriController.xiEoriAlreadyExists().url
      }
    }

    "redirect to Trade With NI page if the logged user has no linked XI EORI" in {

      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(subscriptionDisplayService.getSubscriptionDisplay(any())(any()))
          .thenReturn(Future.successful(Right(subscriptionDisplayResponse.copy(XI_Subscription = None))))
        when(mockGroupEnrolmentExtractor.getEori(any())(any()))
          .thenReturn(Future.successful(existingEori))
        when(mockSessionCache.saveUserAnswers(any())(any()))
          .thenReturn(Future.successful(true))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ApplicationController.onPageLoad().url
        )

        val result = route(application, request).get

        status(result) shouldBe SEE_OTHER

        redirectLocation(
          result
        ).get shouldBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.TradeWithNIController.onPageLoad().url
      }
    }

    "redirect InternalServerError when Subscription Display call fails onPageLoad" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        when(subscriptionDisplayService.getSubscriptionDisplay(any())(any()))
          .thenReturn(Future.successful(Left(ServiceUnavailableResponse)))
        when(mockGroupEnrolmentExtractor.getEori(any())(any()))
          .thenReturn(Future.successful(existingEori))
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ApplicationController.onPageLoad().url
        )

        val result = route(application, request).get
        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
