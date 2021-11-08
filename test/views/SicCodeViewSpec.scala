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

  private implicit val request        = withFakeCSRF(fakeRegisterRequest)
  private val formProvider            = new SicCodeFormProvider
  private def form                    = formProvider.apply()
  private def formWithNoDataError     = form.bind(Map("sic" -> ""))
  private def formWithNonNumericError = form.bind(Map("sic" -> "asdf."))
  private def formWithLengthError     = form.bind(Map("sic" -> "123456"))
  private val sicCodeView             = instanceOf[sic_code].apply(form)
  private val sicCodeViewError        = instanceOf[sic_code].apply(formWithNoDataError)
  private val sicCodeViewDataError    = instanceOf[sic_code].apply(formWithNonNumericError)
  private val sicCodeViewLengthError  = instanceOf[sic_code].apply(formWithLengthError)

  private lazy val sicCodeDoc: Document                = Jsoup.parse(contentAsString(sicCodeView))
  private lazy val sicCodeDocWithError: Document       = Jsoup.parse(contentAsString(sicCodeViewError))
  private lazy val sicCodeDocWithDataError: Document   = Jsoup.parse(contentAsString(sicCodeViewDataError))
  private lazy val sicCodeDocWithLengthError: Document = Jsoup.parse(contentAsString(sicCodeViewLengthError))

  "SIC Code page" should {

    "display correct title" in {
      sicCodeDoc.title must startWith("What is your Standard Industrial Classification (SIC) code?")
    }

    "display correct heading" in {
      sicCodeDoc.body.getElementsByTag("h1").text mustBe "What is your Standard Industrial Classification (SIC) code?"
    }

    "display errors while empty form is submitted" in {
      sicCodeDocWithError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Enter a SIC code"
    }

    "display errors while non-numeric data is submitted" in {
      sicCodeDocWithDataError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Only numerical characters are accepted"
    }

    "display errors while data length not 5 digits " in {
      sicCodeDocWithLengthError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Your SIC Code is limited to 5 numerical characters only"
    }

    "display yes no radio buttons" in {
      sicCodeDoc.body.getElementById("sic").attr("value") mustBe empty
    }

    "display hint" in {
      sicCodeDoc.body.getElementById(
        "sic-hint"
      ).text() mustBe "A SIC code is a 5-digit number that helps HMRC identify what your organisation does. In some countries it is also known as a trade number."
    }

    "display description" in {
      sicCodeDoc.body.getElementById(
        "description"
      ).text() startsWith "If you do not have one, you can search for a relevant SIC code on"
    }
  }
}
