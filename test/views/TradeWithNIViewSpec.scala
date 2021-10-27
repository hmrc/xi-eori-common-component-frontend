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
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.trade_with_ni
import util.ViewSpec

class TradeWithNIViewSpec extends ViewSpec {

  private implicit val request     = withFakeCSRF(fakeRegisterRequest)
  private val formProvider         = new TradeWithNIFormProvider
  private def form                 = formProvider.apply()
  private def formWithError        = form.bind(Map("value" -> ""))
  private val tradeWithNIView      = instanceOf[trade_with_ni].apply(form)
  private val tradeWithNIViewError = instanceOf[trade_with_ni].apply(formWithError)

  private lazy val tradeWithNIDoc: Document          = Jsoup.parse(contentAsString(tradeWithNIView))
  private lazy val tradeWithNIDocWithError: Document = Jsoup.parse(contentAsString(tradeWithNIViewError))

  "Trade With NI page" should {

    "display correct title" in {
      tradeWithNIDoc.title must startWith("Do you move goods in or out of Northern Ireland?")
    }

    "display correct heading" in {
      tradeWithNIDoc.body.getElementsByTag("h1").text mustBe "Do you move goods in or out of Northern Ireland?"
    }

    "display errors while empty form is submitted" in {
      tradeWithNIDocWithError.body.getElementsByClass("govuk-error-summary__list").get(
        0
      ).text mustBe "Tell us if you move goods in or out of Northern Ireland"
    }

    "display yes no radio buttons" in {

      tradeWithNIDoc.body.getElementById("value").attr("checked") mustBe empty
      tradeWithNIDoc.body.getElementById("value-2").attr("checked") mustBe empty
    }

    "display hint" in {
      tradeWithNIDoc.body.getElementById(
        "trade-hint"
      ).text() mustBe "You only need an XI EORI if you move goods from GB (England, Scotland, Wales, the Isle of Man and the Channel Islands) to Northern Ireland."
    }

  }

}
