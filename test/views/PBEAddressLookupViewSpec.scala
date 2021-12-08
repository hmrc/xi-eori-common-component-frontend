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
import play.api.mvc.Request
import play.api.test.Helpers.contentAsString
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.PBEAddressLookupFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.pbe_address_lookup
import util.ViewSpec

class PBEAddressLookupViewSpec extends ViewSpec {

  private val view = instanceOf[pbe_address_lookup]

  implicit val request: Request[Any]       = withFakeCSRF(fakeRegisterRequest)
  private val formProvider                 = new PBEAddressLookupFormProvider()
  private def form                         = formProvider.apply()
  private def formWithNoPostcodeError      = form.bind(Map("line1" -> "Abc", "postcode" -> ""))
  private def formWithInvalidPostcodeError = form.bind(Map("line1" -> "Abc", "postcode" -> "PR11UN"))

  private def formWithline1Error =
    form.bind(Map("line1" -> "Property name or number cannot exceed 35 characters", "postcode" -> "BT11UN"))

  private def doc: Document = Jsoup.parse(contentAsString(view(form)))

  private def noPostCodeErrorDoc: Document = Jsoup.parse(contentAsString(view(formWithNoPostcodeError)))

  private def invalidPostCodeErrorDoc: Document = Jsoup.parse(contentAsString(view(formWithInvalidPostcodeError)))
  private def line1LengthErrorDoc: Document     = Jsoup.parse(contentAsString(view(formWithline1Error)))

  "Address Lookup Postcode page" should {

    "display title for company" in {

      doc.title() must startWith("What is your permanent business establishment address?")
    }

    "display header for company" in {

      doc.body().getElementsByTag("h1").text() mustBe "What is your permanent business establishment address?"
    }

    "display postcode input with label" in {

      doc.body().getElementsByClass("govuk-label postcode").text() mustBe "Postcode"
    }

    "display line 1 input with label" in {

      doc.body().getElementsByClass("govuk-label line1").text() mustBe "Property name or number (optional)"
    }

    "display Find Address button" in {

      doc.body().getElementsByClass("govuk-button").text() mustBe "Find address"
    }

    "display errors while empty postcode is submitted" in {
      noPostCodeErrorDoc.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Enter postal code"
    }

    "display errors while non BT postcode is submitted" in {
      invalidPostCodeErrorDoc.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Postcode must start with BT"
    }

    "display errors while Town Length exceeded" in {
      line1LengthErrorDoc.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Property name or number cannot have more than 35 characters"
    }

  }
}
