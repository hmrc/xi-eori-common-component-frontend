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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ConfirmAddressFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.AddressViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.contact_confirm_address
import util.ViewSpec

class ContactConfirmAddressViewSpec extends ViewSpec {

  implicit val request: Request[Any] = withFakeCSRF(fakeRegisterRequest)
  private val formProvider           = new ConfirmAddressFormProvider()
  private def form                   = formProvider.apply()
  private def formWithError          = form.bind(Map("value" -> ""))
  val address: AddressViewModel      = AddressViewModel("line1", "city", Some("postcode"), "GB")
  private val view                   = instanceOf[contact_confirm_address]
  private val viewError              = instanceOf[contact_confirm_address].apply(address, formWithError)

  private def doc: Document =
    Jsoup.parse(contentAsString(view(address, form)))

  private lazy val docWithError: Document = Jsoup.parse(contentAsString(viewError))

  "Confirm Address page" should {

    "display required title information" in {
      doc.title() must startWith("Is this your XI EORI application contact address?")
    }

    "display correct header" in {
      doc.body().getElementsByTag("h1").text() mustBe "Is this your XI EORI application contact address?"
    }

    "display errors while empty form is submitted" in {
      docWithError.body().getElementsByClass("govuk-error-summary__list").get(
        0
      ).text mustBe "Confirm your address details"
    }

    "display heading for confirming the address " in {
      doc.body().getElementsByClass("govuk-fieldset__legend").get(0).text mustBe "Is this address correct?"
    }

    "display option for confirming the address" in {
      doc.body().getElementsByClass("govuk-radios__label").get(0).text mustBe "Yes, this address is correct"
    }

    "display option for changing the address" in {
      doc.body().getElementsByClass("govuk-radios__label").get(1).text mustBe "No, I want to change the address"
    }

    "display option for changing the address manually" in {
      doc.body().getElementsByClass("govuk-radios__label").get(2).text mustBe "I want to enter the address manually"
    }

    "display continue button" in {
      doc.body().getElementsByClass("govuk-button").get(0).text mustBe "Continue"
    }

    "display back button" in {
      doc.body().getElementsByClass("govuk-back-link").get(0).text mustBe "Back"
    }

    "display get Help link" in {
      val helpAndSupport = doc.body().getElementById("helpAndSupport")
      helpAndSupport.getElementsByTag("a").get(0).text mustBe "Get help with this page."
    }
  }
}
