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
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.unauthorized
import util.ViewSpec

class UnAuthorizedViewSpec extends ViewSpec {

  private implicit val request = withFakeCSRF(fakeAtarRegisterRequest)
  private val unAuthorizedView = instanceOf[unauthorized]

  "UnAuthorized page" should {

    "display correct title" in {
      standardOrgDoc.title must startWith("Service unavailable")
    }

    "display correct heading" in {
      standardOrgDoc.body.getElementsByTag("h1").text mustBe "Service unavailable"
    }

    "display para-1" in {
      standardOrgDoc.body
        .getElementById("para-1")
        .text mustBe "You may not have access to this service."

    }
    "display para-2" in {
      standardOrgDoc.body.getElementById("para-2").text mustBe "It is currently only available by invitation."
    }

    lazy val standardOrgDoc: Document = Jsoup.parse(contentAsString(unAuthorizedView()))

  }
}
