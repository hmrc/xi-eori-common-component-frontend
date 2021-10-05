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

import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.EnrolmentAlreadyExistsController
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{registration_exists, registration_exists_group}
import common.pages.RegistrationPage
import util.ControllerSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.{AuthActionMock, SessionBuilder}

class EnrolmentAlreadyExistsControllerSpec extends ControllerSpec with AuthActionMock {

  private val registrationExistsView      = instanceOf[registration_exists]
  private val registrationExistsGroupView = instanceOf[registration_exists_group]
  private val mockAuthConnector           = mock[AuthConnector]
  private val mockAuthAction              = authAction(mockAuthConnector)

  val controller =
    new EnrolmentAlreadyExistsController(mockAuthAction, registrationExistsView, registrationExistsGroupView, mcc)

  val paragraphXpath = "//*[@id='para1']"

  "Enrolment already exists controller" should {

    "redirect to the enrolment already exists page" in {
      running(application) {

        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val result =
          await(
            controller.enrolmentAlreadyExists(atarService).apply(
              SessionBuilder.buildRequestWithSessionAndPath("/atar/", defaultUserId)
            )
          )

        status(result) shouldBe OK

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("There is a problem")

      }

    }

    "redirect to the enrolment already exists for group page" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val result =
          await(
            controller.enrolmentAlreadyExistsForGroup(atarService).apply(
              SessionBuilder.buildRequestWithSessionAndPath("/atar/", defaultUserId)
            )
          )

        status(result) shouldBe OK

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("There is a problem")

      }
    }
  }
}
