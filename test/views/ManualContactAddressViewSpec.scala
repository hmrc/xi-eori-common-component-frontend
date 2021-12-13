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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ManualContactAddressFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.services.countries.Countries
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.manual_contact_address
import util.ViewSpec

class ManualContactAddressViewSpec extends ViewSpec {

  implicit val request: Request[Any] = withFakeCSRF(fakeRegisterRequest)
  private val formProvider           = new ManualContactAddressFormProvider()
  private def form                   = formProvider.apply()
  private def formWithNoDataError    = form.bind(Map("line1" -> "", "townorcity" -> "", "postcode" -> ""))

  private def formWithNoLine1Error =
    form.bind(Map("line1" -> "", "townorcity" -> "test", "postcode" -> "BT11AA", "countryCode" -> "GB"))

  private def formWithNoTownError =
    form.bind(Map("line1" -> "Abc", "townorcity" -> "", "postcode" -> "BT11AA", "countryCode" -> "GB"))

  private def formWithNoCountryError =
    form.bind(Map("line1" -> "Abc", "townorcity" -> "test", "postcode" -> "BT11AA"))

  private def formWithLine1LenError =
    form.bind(
      Map(
        "line1"       -> "This Address line in PBE Address page cannot exceed more than 70 characters",
        "townorcity"  -> "test",
        "postcode"    -> "BT11UN",
        "countryCode" -> "GB"
      )
    )

  private def formWithLine2LenError =
    form.bind(
      Map(
        "line1"       -> "This Address line 1",
        "townorcity"  -> "test",
        "postcode"    -> "BT11UN",
        "countryCode" -> "GB",
        "line2"       -> "This Address line 2 cannot exceed more than 35 characters"
      )
    )

  private def formWithRegionLenError =
    form.bind(
      Map(
        "line1"         -> "This Address line 1",
        "townorcity"    -> "test",
        "postcode"      -> "BT11UN",
        "countryCode"   -> "GB",
        "line2"         -> "This Address line 2",
        "regionorstate" -> "This Region or state cannot exceed more than 35 characters"
      )
    )

  private def formWithTownLenError =
    form.bind(
      Map(
        "line1"       -> "Abc",
        "townorcity"  -> "Town or city cannot exceed 35 characters",
        "postcode"    -> "BT11UN",
        "countryCode" -> "GB"
      )
    )

  val (countries, picker) = Countries.getCountryParametersForAllCountries()

  private val view             = instanceOf[manual_contact_address].apply(form, countries, picker)
  private val viewError        = instanceOf[manual_contact_address].apply(formWithNoDataError, countries, picker)
  private val viewLine1Error   = instanceOf[manual_contact_address].apply(formWithNoLine1Error, countries, picker)
  private val viewTownError    = instanceOf[manual_contact_address].apply(formWithNoTownError, countries, picker)
  private val viewCountryError = instanceOf[manual_contact_address].apply(formWithNoCountryError, countries, picker)

  private val viewLine1LengthError = instanceOf[manual_contact_address].apply(formWithLine1LenError, countries, picker)
  private val viewLine2LengthError = instanceOf[manual_contact_address].apply(formWithLine2LenError, countries, picker)
  private val viewTownLengthError  = instanceOf[manual_contact_address].apply(formWithTownLenError, countries, picker)

  private val viewRegionLengthError =
    instanceOf[manual_contact_address].apply(formWithRegionLenError, countries, picker)

  private lazy val doc: Document               = Jsoup.parse(contentAsString(view))
  private lazy val docWithError: Document      = Jsoup.parse(contentAsString(viewError))
  private lazy val docWithLine1Error: Document = Jsoup.parse(contentAsString(viewLine1Error))
  private lazy val docWithTownError: Document  = Jsoup.parse(contentAsString(viewTownError))
  private lazy val docCountryError: Document   = Jsoup.parse(contentAsString(viewCountryError))

  private lazy val docLine1LenError: Document  = Jsoup.parse(contentAsString(viewLine1LengthError))
  private lazy val docLine2LenError: Document  = Jsoup.parse(contentAsString(viewLine2LengthError))
  private lazy val docTownLenError: Document   = Jsoup.parse(contentAsString(viewTownLengthError))
  private lazy val docRegionLenError: Document = Jsoup.parse(contentAsString(viewRegionLengthError))

  "Manual Contact Address page" should {

    "display title for company" in {

      doc.title must startWith("What is your XI EORI application contact address?")
    }

    "display header for Manual Contact" in {

      doc.body.getElementsByTag("h1").text mustBe "What is your XI EORI application contact address?"
    }

    "display Address line 1 input with label" in {

      doc.body.getElementsByClass("govuk-label line1").text() mustBe "Address line 1"
    }

    "display Address line 2 input with label" in {

      doc.body.getElementsByClass("govuk-label line2").text() mustBe "Address line 2 (optional)"
    }

    "display Town or city input with label" in {

      doc.body.getElementsByClass("govuk-label town").text() mustBe "Town or city"
    }

    "display postcode input with label" in {

      doc.body.getElementsByClass("govuk-label postcode").text() mustBe "Postcode (optional)"
    }

    "Continue button" in {

      doc.body.getElementsByClass("govuk-button").text() mustBe "Continue"
    }

    "display errors while empty form is submitted" in {
      docWithError.body.getElementsByClass("govuk-error-summary__title").get(0)
        .text mustBe "There is a problem"
    }

    "display errors while empty line 1 is submitted" in {
      docWithLine1Error.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Enter Address line 1"
    }

    "display errors while empty town is submitted" in {
      docWithTownError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Enter town or city"
    }

    "display errors while empty postcode is submitted" in {
      docCountryError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Select Country"
    }

    "display errors while Line 1 Length exceeded" in {
      docLine1LenError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "The first line of the address must be 35 characters or less"
    }

    "display errors while Line 2 Length exceeded" in {
      docLine2LenError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "The second line of the address must be 34 characters or less"
    }

    "display errors while Town Length exceeded" in {
      docTownLenError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Town or city must be 35 characters or less"
    }

    "display errors while Region Length exceeded" in {
      docRegionLenError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Region or state must be 35 characters or less"
    }

  }
}
