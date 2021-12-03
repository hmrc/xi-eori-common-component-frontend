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
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.address_lookup_no_results
import util.ViewSpec

class AddressLookupNoResultsViewSpec extends ViewSpec {

  private val view = instanceOf[address_lookup_no_results]

  implicit val request: Request[Any] = withFakeCSRF(fakeRegisterRequest)

  private val postcode = "AA11 1AA"

  private val doc: Document = Jsoup.parse(contentAsString(view(postcode)))

  private val reviewDoc: Document = Jsoup.parse(contentAsString(view(postcode)))

  "Address lookup error page" should {

    "display title" in {

      doc.title() must startWith("No address found")
    }

    "display header" in {

      doc.body().getElementsByTag("h1").text() mustBe "No address found"
    }

    "display hint" in {

      doc.body().getElementById(
        "hint"
      ).text() mustBe "We are unable to find an address associated with this postcode. Review the postcode below or enter your address manually."
    }

    "display change postcode link" in {

      val reenterPostcodeButton = doc.body().getElementById("reenter-postcode-button")

      reenterPostcodeButton.text() mustBe "Change"
      reenterPostcodeButton.attr("href") mustBe "/xi-customs-registration-services/pbe-postcode"
    }

    "display enter manually address link" in {

      val enterManuallyAddressLink = doc.body().getElementById("enter-manually-button")

      enterManuallyAddressLink.text() mustBe "I want to enter my address manually."
      enterManuallyAddressLink.attr("href") mustBe "/xi-customs-registration-services/pbe-company-address"
    }
  }
}
