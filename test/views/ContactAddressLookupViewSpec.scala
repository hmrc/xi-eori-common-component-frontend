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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ContactAddressLookupFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.contact_address_lookup
import util.ViewSpec

class ContactAddressLookupViewSpec extends ViewSpec {

  private val view = instanceOf[contact_address_lookup]

  implicit val request: Request[Any] = withFakeCSRF(fakeRegisterRequest)
  private val formProvider           = new ContactAddressLookupFormProvider()
  private def form                   = formProvider.apply()

  private def doc: Document =
    Jsoup.parse(contentAsString(view(form)))

  "Contact Lookup Postcode page" should {

    "display title for Contact Address" in {

      doc.title() must startWith("What is your XI EORI application contact address?")
    }

    "display header for Contact Address" in {

      doc.body().getElementsByTag("h1").text() mustBe "What is your XI EORI application contact address?"
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

    "display manual address link" in {

      val manualAddressLink = doc.body().getElementById("cannot-find-address")

      manualAddressLink.text() mustBe "Contact address is outside the UK"
      manualAddressLink.attr("href") mustBe "/xi-customs-registration-services/pbe-company-address"
    }

  }
}
