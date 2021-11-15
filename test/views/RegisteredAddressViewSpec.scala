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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.PBEAddressResultsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.AddressLookup
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.PBEAddressLookup
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.registered_address
import util.ViewSpec

class RegisteredAddressViewSpec extends ViewSpec {

  private val view = instanceOf[registered_address]

  private implicit val request = withFakeCSRF(fakeRegisterRequest)

  private val params         = PBEAddressLookup("AA11 1AA", Some("Flat 1"))
  private val allowedAddress = Seq(AddressLookup("Line 1", "City", "BB11 1BB", "GB"))

  private val form = PBEAddressResultsFormProvider.form(allowedAddress.map(_.dropDownView))

  private val formWithError =
    PBEAddressResultsFormProvider.form(allowedAddress.map(_.dropDownView)).bind(Map("address" -> "invalid"))

  private val doc: Document =
    Jsoup.parse(contentAsString(view(form, params, allowedAddress)))

  private val docWithoutLine1: Document =
    Jsoup.parse(contentAsString(view(form, params.copy(line1 = None), allowedAddress)))

  private val docWithErrorSummary =
    Jsoup.parse(contentAsString(view(formWithError, params, allowedAddress)))

  "Address Lookup Postcode page" should {

    "display title for company" in {

      doc.title() must startWith("What is your registered company address?")
    }

    "display summary of params" in {

      val postcode = doc.body().getElementsByClass("review-tbl__postcode").get(0)
      postcode.getElementsByClass("govuk-summary-list__key").text mustBe "Postcode"
      postcode.getElementsByClass("govuk-summary-list__value").text mustBe "AA11 1AA"

      val line1 = doc.body().getElementsByClass("review-tbl__line1").get(0)
      line1.getElementsByClass("govuk-summary-list__key").text mustBe "Property name or number"
      line1.getElementsByClass("govuk-summary-list__value").text mustBe "Flat 1"
    }

    "display summary of params with 'Not known' for property name or number" in {
      val line1 = docWithoutLine1.body().getElementsByClass("review-tbl__line1").get(0)
      line1.getElementsByClass("govuk-summary-list__key").text mustBe "Property name or number"
      line1.getElementsByClass("govuk-summary-list__value").text mustBe "Not Known"
    }

    "display change link to params page" in {

      val postcodeChangeLink = doc.body().getElementsByClass("review-tbl__postcode_change").get(0)
      val line1ChangeLink    = doc.body().getElementsByClass("review-tbl__line1_change").get(0)

      postcodeChangeLink.getElementsByTag("a").text() must startWith("Change")
      postcodeChangeLink.getElementsByTag("a").attr("href") mustBe "/xi-customs-registration-services/pbe-postcode"

      line1ChangeLink.getElementsByTag("a").text() must startWith("Change")
      line1ChangeLink.getElementsByTag("a").attr("href") mustBe "/xi-customs-registration-services/pbe-postcode"
    }

    "display dropdown with label" in {

      doc.body().getElementsByTag("label").text() mustBe "Select your address"

      val dropdown = doc.body().getElementsByTag("select").get(0)

      dropdown.getElementsByTag("option").get(0).text() mustBe empty
      dropdown.getElementsByTag("option").get(1).text() mustBe "Line 1, City, BB11 1BB"
    }

    "display manual address link" in {

      val manualAddressLink = doc.body().getElementById("cannot-find-address")

      manualAddressLink.text() mustBe "I can't find my address in the list"
      manualAddressLink.attr("href") mustBe "#"
    }

    "display Continue button" in {

      doc.body().getElementsByClass("govuk-button").text() mustBe "Continue"
    }

    "display error summary" in {

      docWithErrorSummary.getElementById("error-summary-title").text() mustBe "There is a problem"
      docWithErrorSummary.getElementsByClass(
        "govuk-error-summary__list"
      ).text() mustBe "Select you address from the list"
    }
  }
}
