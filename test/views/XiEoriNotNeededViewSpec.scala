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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.TradeWithNIFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{trade_with_ni, xi_eori_not_needed}
import util.ViewSpec

class XiEoriNotNeededViewSpec extends ViewSpec {

  private implicit val request = withFakeCSRF(fakeRegisterRequest)

  private val XiEoriNotNeededView               = instanceOf[xi_eori_not_needed]
  private lazy val XiEoriNotNeededDoc: Document = Jsoup.parse(contentAsString(XiEoriNotNeededView()))

  "You cannot use this service page for users of type standard org" should {

    "display correct title" in {
      XiEoriNotNeededDoc.title must startWith("You do not need an XI EORI number")
    }

    "display correct heading" in {
      XiEoriNotNeededDoc.body.getElementsByTag("h1").text mustBe "You do not need an XI EORI number"
    }

    "display correct details in the list" in {
      XiEoriNotNeededDoc.body.getElementById(
        "xi-eori-list-heading"
      ).text() mustBe "You do not need an XI EORI. This is because either:"
      XiEoriNotNeededDoc.body.getElementById(
        "xi-eori-bullet1"
      ).text() mustBe "you do not move goods in or out of Northern Ireland"
      XiEoriNotNeededDoc.body.getElementById(
        "xi-eori-bullet2"
      ).text() mustBe "you have an EORI number issued in the EU (you can use this number to move your goods)"
    }

  }

}
