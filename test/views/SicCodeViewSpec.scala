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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.SicCodeFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.sic_code
import util.ViewSpec

class SicCodeViewSpec extends ViewSpec {

  private implicit val request = withFakeCSRF(fakeRegisterRequest)
  private val formProvider     = new SicCodeFormProvider
  private def form             = formProvider.apply()
  private def formWithError    = form.bind(Map("value" -> ""))
  private val sicCodeView      = instanceOf[sic_code].apply(form)
  private val sicCodeViewError = instanceOf[sic_code].apply(formWithError)

  private lazy val sicCodeDoc: Document          = Jsoup.parse(contentAsString(sicCodeView))
  private lazy val sicCodeDocWithError: Document = Jsoup.parse(contentAsString(sicCodeViewError))

  "SIC Code page" should {

    "display correct title" in {
      sicCodeDoc.title must startWith("What is your Standard Industrial Classification (SIC) code?")
    }

    "display correct heading" in {
      sicCodeDoc.body.getElementsByTag("h1").text mustBe "What is your Standard Industrial Classification (SIC) code?"
    }

    /*"display errors while empty form is submitted" in {
      sicCodeDocWithError.body.getElementsByClass("govuk-error-summary__list").get(0).text mustBe "Enter a SIC code"
    }*/

    "display yes no radio buttons" in {
      sicCodeDoc.body.getElementById("sic").attr("value") mustBe empty
    }

    "display hint" in {
      sicCodeDoc.body.getElementById(
        "sic-hint"
      ).text() mustBe "A SIC code is a 5-digit number that helps HMRC identify what your organisation does. In some countries it is also known as a trade number."
    }

  }

}
