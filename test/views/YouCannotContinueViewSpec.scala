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

package views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.auth.core.AffinityGroup.{Agent, Organisation}
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{you_cannot_continue, you_cant_use_service}
import util.ViewSpec

class YouCannotContinueViewSpec extends ViewSpec {

  private implicit val request      = withFakeCSRF(fakeRegisterRequest)
  private val youCantCannotView = instanceOf[you_cannot_continue]

  "You cannot continue page" should {

    "display correct title" in {
      youCannotContinueDoc.title must startWith("We're sorry you cannot continue with your registration")
    }

    "display correct heading" in {
      youCannotContinueDoc.body.getElementsByTag("h1").text mustBe "We're sorry you cannot continue with your registration"
    }

    "display list heading and details" in {
      youCannotContinueDoc.body
        .getElementById("details-heading")
        .text mustBe "You have told us that the details we hold about you are incorrect. You need to contact one of the following to correct the information before you can continue:"

     youCannotContinueDoc.body
       .getElementById("change-details-text1")
       .text mustBe "Companies house (organisations only)"
     youCannotContinueDoc.body
       .getElementById("change-details-text2")
       .text mustBe "HMRC VAT"
     youCannotContinueDoc.body
       .getElementById("change-details-text3")
       .text mustBe "Your personal tax account (sole traders and individuals)"
    }
  }


  private lazy val youCannotContinueDoc: Document = Jsoup.parse(contentAsString(youCantCannotView()))

}
