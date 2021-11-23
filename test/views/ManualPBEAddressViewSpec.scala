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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ManualPBEAddressFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.manual_pbe_address
import util.ViewSpec

class ManualPBEAddressViewSpec extends ViewSpec {

  private val view = instanceOf[manual_pbe_address]

  implicit val request: Request[Any]  = withFakeCSRF(fakeRegisterRequest)
  private val formProvider            = new ManualPBEAddressFormProvider()
  private def form                    = formProvider.apply()
  private def formWithNoDataError     = form.bind(Map("line1" -> "", "townorcity" -> "", "postcode" -> ""))
  private def formWithNoLine1Error    = form.bind(Map("line1" -> "", "townorcity" -> "test", "postcode" -> "BT11AA"))
  private def formWithNoTownError     = form.bind(Map("line1" -> "Abc", "townorcity" -> "", "postcode" -> "BT11AA"))
  private def formWithNoPostcodeError = form.bind(Map("line1" -> "Abc", "townorcity" -> "test", "postcode" -> ""))

  private def formWithInvalidPostcodeError =
    form.bind(Map("line1" -> "Abc", "townorcity" -> "test", "postcode" -> "PR11UN"))

  private def formWithLine1LenError =
    form.bind(
      Map(
        "line1"      -> "This Address line in PBE Address page cannot exceed more than 70 characters",
        "townorcity" -> "test",
        "postcode"   -> "BT11UN"
      )
    )

  private def formWithTownLenError =
    form.bind(Map("line1" -> "Abc", "townorcity" -> "Town or city cannot exceed 35 characters", "postcode" -> "BT11UN"))

  private val manualPBEAddressView           = instanceOf[manual_pbe_address].apply(form)
  private val manualPBEAddressViewError      = instanceOf[manual_pbe_address].apply(formWithNoDataError)
  private val manualPBEAddressViewLine1Error = instanceOf[manual_pbe_address].apply(formWithNoLine1Error)
  private val manualPBEAddressViewTownError  = instanceOf[manual_pbe_address].apply(formWithNoTownError)
  private val manualPBEAddressPostCodeError  = instanceOf[manual_pbe_address].apply(formWithNoPostcodeError)
  private val manualPBENonBTPostCodeError    = instanceOf[manual_pbe_address].apply(formWithInvalidPostcodeError)
  private val manualPBELineLengthError       = instanceOf[manual_pbe_address].apply(formWithLine1LenError)
  private val manualPBETownLengthError       = instanceOf[manual_pbe_address].apply(formWithTownLenError)

  private lazy val manualPBEDoc: Document               = Jsoup.parse(contentAsString(manualPBEAddressView))
  private lazy val manualPBEDocWithError: Document      = Jsoup.parse(contentAsString(manualPBEAddressViewError))
  private lazy val manualPBEDocWithLine1Error: Document = Jsoup.parse(contentAsString(manualPBEAddressViewLine1Error))
  private lazy val manualPBEDocWithTownError: Document  = Jsoup.parse(contentAsString(manualPBEAddressViewTownError))
  private lazy val manualPBEDocPostCodeError: Document  = Jsoup.parse(contentAsString(manualPBEAddressPostCodeError))
  private lazy val manualPBEDocNonBTPostError: Document = Jsoup.parse(contentAsString(manualPBENonBTPostCodeError))
  private lazy val manualPBEDocLine1LenError: Document  = Jsoup.parse(contentAsString(manualPBELineLengthError))
  private lazy val manualPBEDocTownLenError: Document   = Jsoup.parse(contentAsString(manualPBETownLengthError))

  "Manual PBE Address page" should {

    "display title for company" in {

      manualPBEDoc.title must startWith("What is your permanent business establishment address?")
    }

    "display header for Manual PBE" in {

      manualPBEDoc.body.getElementsByTag("h1").text mustBe "What is your permanent business establishment address?"
    }

    "display Address line 1 input with label" in {

      manualPBEDoc.body.getElementsByClass("govuk-label line1").text() mustBe "Address line 1"
    }

    "display Town or city input with label" in {

      manualPBEDoc.body.getElementsByClass("govuk-label town").text() mustBe "Town or city"
    }

    "display postcode input with label" in {

      manualPBEDoc.body.getElementsByClass("govuk-label postcode").text() mustBe "Postcode"
    }

    "display country with text" in {
      manualPBEDoc.body.getElementsByClass("govuk-select").text() mustBe "United Kingdom"
    }

    "Continue button" in {

      manualPBEDoc.body.getElementsByClass("govuk-button").text() mustBe "Continue"
    }

    "display errors while empty form is submitted" in {
      manualPBEDocWithError.body.getElementsByClass("govuk-error-summary__title").get(0)
        .text mustBe "There is a problem"
    }

    "display errors while empty line 1 is submitted" in {
      manualPBEDocWithLine1Error.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Enter Address line 1"
    }

    "display errors while empty town is submitted" in {
      manualPBEDocWithTownError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Enter town or city"
    }

    "display errors while empty postcode is submitted" in {
      manualPBEDocPostCodeError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Enter postcode"
    }

    "display errors while non BT postcode is submitted" in {
      manualPBEDocNonBTPostError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Postcode must start with BT"
    }

    "display errors while Line 1 Length exceeded" in {
      manualPBEDocLine1LenError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "The first line of the address must be 70 characters or less"
    }

    "display errors while Town Length exceeded" in {
      manualPBEDocTownLenError.body.getElementsByClass("govuk-list govuk-error-summary__list").get(0)
        .text mustBe "Town or city must be 35 characters or less"
    }

  }
}
