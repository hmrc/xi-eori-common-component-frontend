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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.AddressResultsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.AddressLookup
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ContactAddressLookup
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.contact_address_results
import util.ViewSpec

class ContactAddressResultViewSpec extends ViewSpec {

  private val view = instanceOf[contact_address_results]

  private implicit val request: Request[AnyContentAsEmpty.type] = withFakeCSRF(fakeRegisterRequest)

  private val params         = ContactAddressLookup("AA11 1AA", Some("Flat 1"))
  private val allowedAddress = Seq(AddressLookup("Line 1", "City", "BB11 1BB", "GB"))

  private val form = AddressResultsFormProvider.form(allowedAddress.map(_.dropDownView))

  private val formWithError =
    AddressResultsFormProvider.form(allowedAddress.map(_.dropDownView)).bind(Map("address" -> "invalid"))

  private val doc: Document =
    Jsoup.parse(contentAsString(view(form, params, allowedAddress)))

  private val docWithoutLine1: Document =
    Jsoup.parse(contentAsString(view(form, params.copy(line1 = None), allowedAddress)))

  private val docWithErrorSummary =
    Jsoup.parse(contentAsString(view(formWithError, params, allowedAddress)))

  "Contact Address Lookup Postcode page" should {

    "display title for company" in {

      doc.title() must startWith("What is your XI EORI application contact address?")
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
      line1.getElementsByClass("govuk-summary-list__value").text mustBe "Not known"
    }

    "display change link to params page" in {

      val postcodeChangeLink = doc.body().getElementsByClass("review-tbl__postcode_change").get(0)
      val line1ChangeLink    = doc.body().getElementsByClass("review-tbl__line1_change").get(0)

      postcodeChangeLink.getElementsByTag("a").text() must startWith("Change")
      postcodeChangeLink.getElementsByTag("a").attr("href") mustBe "/xi-customs-registration-services/contact-postcode"

      line1ChangeLink.getElementsByTag("a").text() must startWith("Change")
      line1ChangeLink.getElementsByTag("a").attr("href") mustBe "/xi-customs-registration-services/contact-postcode"
    }

    "display dropdown with label" in {

      doc.body().getElementsByTag("label").text() mustBe "Select your address"

      val dropdown = doc.body().getElementsByTag("select").get(0)

      dropdown.getElementsByTag("option").get(0).text() mustBe empty
      dropdown.getElementsByTag("option").get(1).text() mustBe "Line 1, City, BB11 1BB"
    }

    "display Continue button" in {

      doc.body().getElementsByClass("govuk-button").text() mustBe "Continue"
    }

    "display hint" in {

      doc.body().getElementsByClass("govuk-hint").text() mustBe "Choose your address below"
    }

    "display error summary" in {

      docWithErrorSummary.getElementById("error-summary-title").text() mustBe "There is a problem"
      docWithErrorSummary.getElementsByClass(
        "govuk-error-summary__list"
      ).text() mustBe "Select your address from the list"
    }
  }
}
