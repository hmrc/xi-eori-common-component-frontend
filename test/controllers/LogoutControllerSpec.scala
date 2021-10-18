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
import org.mockito.Mockito.{verify, when}
import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.{inject, Application}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.GroupEnrolmentExtractor
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

import java.util.UUID
import scala.concurrent.Future

class LogoutControllerSpec extends WordSpec with Matchers {
  val defaultUserId: String = s"user-${UUID.randomUUID}"
  val mockAuthConnector     = mock[AuthConnector]

  val application: Application =
    new GuiceApplicationBuilder().overrides(inject.bind[AuthConnector].to(mockAuthConnector)).configure(
      "metrics.jvm"     -> false,
      "metrics.enabled" -> false
    )
      .build()

  "LogoutController" should {
    "return 303 when logout button is clicked" in {
      withAuthorisedUser(defaultUserId, mockAuthConnector)
      val request = SessionBuilder.buildRequestWithSessionAndPath(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.logout().url,
        defaultUserId
      )
      val result = route(application, request).get
      status(result) shouldBe SEE_OTHER

    }

    "redirect to start page when user logout" in {
      withAuthorisedUser(defaultUserId, mockAuthConnector)
      val request = SessionBuilder.buildRequestWithSessionAndPath(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.logout().url,
        defaultUserId
      )
      val result = route(application, request).get
      redirectLocation(result) shouldBe Some("http://localhost:9553/bas-gateway/sign-out-without-state")

    }

    "return Ok 303 when signOut method is requested" in {
      withAuthorisedUser(defaultUserId, mockAuthConnector)
      val request = SessionBuilder.buildRequestWithSessionAndPath(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.timeout().url,
        defaultUserId
      )
      val result = route(application, request).get
      status(result) shouldBe SEE_OTHER

    }

    "return Ok 200 when displayPage method is requested" in {

      withAuthorisedUser(defaultUserId, mockAuthConnector)
      val request = SessionBuilder.buildRequestWithSessionAndPath(
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage().url,
        defaultUserId
      )
      val result = route(application, request).get
      val page   = RegistrationPage(contentAsString(result))
      page.title should startWith("For your security, we signed you out")

    }

  }
}
