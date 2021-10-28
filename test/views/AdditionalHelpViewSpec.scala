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
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.helpers.additional_help
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.you_cannot_continue
import util.ViewSpec

class AdditionalHelpViewSpec extends ViewSpec {

  private implicit val request = withFakeCSRF(fakeRegisterRequest)
  private val additionalHelpView = instanceOf[additional_help]

  "Additional Help page" should {

    "display correct heading" in {
      additionalHelpDoc.body.getElementsByTag("h2").text mustBe "If you need help"
    }

    "display telephone and working hours" in {
      additionalHelpDoc.body
        .getElementById("telephone")
        .text mustBe "Telephone: 0300 322 7067"
      additionalHelpDoc.body
        .getElementById("working-hours")
        .text mustBe "Monday to Friday, 8am to 6pm (except public holidays)"
      additionalHelpDoc.body
        .getElementById("call-charges")
        .text mustBe "Find out about call charges"

    }
  }


  private lazy val additionalHelpDoc: Document = Jsoup.parse(contentAsString(additionalHelpView()))

}
