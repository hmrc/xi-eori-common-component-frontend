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
import play.api.mvc.{AnyContentAsEmpty, Request}
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.already_have_xi_eori
import util.ViewSpec

class AlreadyHaveXIEoriViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContentAsEmpty.type] = withFakeCSRF(fakeRegisterRequest)

  private val alreadyhaveXIEoriView               = instanceOf[already_have_xi_eori]
  private lazy val alreadyhaveXIEoriDoc: Document = Jsoup.parse(contentAsString(alreadyhaveXIEoriView("XI8989989797")))

  "Already Have XI Eori page " should {

    "display correct title" in {
      alreadyhaveXIEoriDoc.title must startWith("You already have an XI EORI connected to this Government Gateway")
    }

    "display correct heading" in {
      alreadyhaveXIEoriDoc.body.getElementsByTag(
        "h1"
      ).text mustBe "You already have an XI EORI connected to this Government Gateway"
    }

    "display XI EORI details in the para" in {
      alreadyhaveXIEoriDoc.body.getElementById("para1").text() startsWith "Your XI EORI number is: "

    }

  }

}
