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
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.address_lookup_error
import util.ViewSpec

class AddressLookupErrorViewSpec extends ViewSpec {

  private val view = instanceOf[address_lookup_error]

  implicit val request: Request[Any] = withFakeCSRF(fakeRegisterRequest)

  private val doc: Document                = Jsoup.parse(contentAsString(view(isPBEAddressLookupFailed = true)))
  private val contactAddressdDoc: Document = Jsoup.parse(contentAsString(view(isPBEAddressLookupFailed = false)))

  "Address lookup error page" should {

    "display title" in {

      doc.title() must startWith("We have a problem")
    }

    "display header" in {

      doc.body().getElementsByTag("h1").text() mustBe "We have a problem"
    }

    "display hint" in {

      doc.body().getElementById(
        "hint"
      ).text() mustBe "We are unable to view the list of matching addresses at this time. Try again in a few minutes or enter your address manually."
    }

    "display change postcode link" in {

      val reenterPostcodeButton = doc.body().getElementsByClass("reenter-postcode-button")

      reenterPostcodeButton.text() mustBe "Re-enter postcode"
      reenterPostcodeButton.attr("href") mustBe "/xi-customs-registration-services/pbe-postcode"
    }
    "display change postcode link when searching contact address" in {

      val reenterPostcodeButton = contactAddressdDoc.body().getElementsByClass("reenter-postcode-button")

      reenterPostcodeButton.text() mustBe "Re-enter postcode"
      reenterPostcodeButton.attr("href") mustBe "/xi-customs-registration-services/contact-postcode"
    }

    "display enter manually address link" in {

      val enterManuallyAddressLink = doc.body().getElementById("enter-manually-button")

      enterManuallyAddressLink.text() mustBe "I want to enter my address manually."
      enterManuallyAddressLink.attr("href") mustBe "/xi-customs-registration-services/pbe-company-address"
    }
  }

}
