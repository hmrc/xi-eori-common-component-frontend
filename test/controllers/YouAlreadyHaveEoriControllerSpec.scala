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
import play.api.Application
import play.api.test.Helpers._
import util.ControllerSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

class YouAlreadyHaveEoriControllerSpec extends ControllerSpec {

  val paragraphXpath = "//*[@id='para1']"
  "Enrolment already exists controller" should {
    "redirect to the enrolment already exists page" in {
      running(application) {

        withAuthorisedUser(defaultUserId, mockAuthConnector)

        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.YouAlreadyHaveEoriController.eoriAlreadyExists().url,
          defaultUserId
        )
        val result = route(application, request).get
        status(result) shouldBe OK

        val page = RegistrationPage(contentAsString(result))

        page.title should startWith("There is a problem")

      }

    }

  }
}
