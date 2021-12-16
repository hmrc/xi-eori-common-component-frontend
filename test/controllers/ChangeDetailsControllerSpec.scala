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
import play.api.test.Helpers._
import util.BaseSpec
import util.builders.AuthBuilder.withAuthorisedUser
import util.builders.SessionBuilder

class ChangeDetailsControllerSpec extends BaseSpec {
  val callChargesXpath = "//*[@id='call-charges']"
  "Change Details controller" should {
    "you cannot continue page" in {
      running(application) {
        withAuthorisedUser(defaultUserId, mockAuthConnector)
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ChangeDetailsController.incorrectDetails().url
        )
        val result = route(application, request).get
        status(result) shouldBe OK
        val page = RegistrationPage(contentAsString(result))
        page.title should startWith("We're sorry you cannot continue with your registration")
      }

    }

    "redirect to call charges when user clicks on Find out about call charges" in {

      running(application) {

        withAuthorisedUser(defaultUserId, mockAuthConnector)
        val request = SessionBuilder.buildRequestWithSessionAndPath(
          uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ChangeDetailsController.incorrectDetails().url
        )
        val result = route(application, request).get
        val page   = RegistrationPage(contentAsString(result))
        page.getElementsHref(callChargesXpath) shouldBe "https://www.gov.uk/call-charges"
      }

    }

  }
}
