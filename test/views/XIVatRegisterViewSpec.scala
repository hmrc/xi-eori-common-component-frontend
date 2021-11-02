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
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.xi_vat_register
import util.ViewSpec

class XIVatRegisterViewSpec extends ViewSpec {

  private implicit val request = withFakeCSRF(fakeRegisterRequest)

  private val XiVatRegisterView               = instanceOf[xi_vat_register]
  private lazy val XiVatRegisterDoc: Document = Jsoup.parse(contentAsString(XiVatRegisterView()))

  "XI VAT Register page " should {

    "display correct title" in {
      XiVatRegisterDoc.title must startWith("Registering for an XI VAT number")
    }

    "display correct heading" in {
      XiVatRegisterDoc.body.getElementsByTag("h1").text mustBe "Registering for an XI VAT number"
    }

    "display correct details in the list" in {
      XiVatRegisterDoc.body.getElementById(
        "details-heading"
      ).text() mustBe "As a VAT-registered business, you need to register to use an XI VAT number if any of the following apply:"
      XiVatRegisterDoc.body.getElementById(
        "xi-vat-register-text1"
      ).text() mustBe "your goods are located in Northern Ireland at the time of sale"
      XiVatRegisterDoc.body.getElementById(
        "xi-vat-register-text2"
      ).text() mustBe "you receive goods in Northern Ireland from VAT-registered EU businesses for business purposes"
      XiVatRegisterDoc.body.getElementById(
        "xi-vat-register-text3"
      ).text() mustBe "you sell or move goods from Northern Ireland to an EU country"
    }

    "display XI VAT register link if XI Vat number is not present" in {
      XiVatRegisterDoc.body.getElementById(
        "vat-register-link"
      ).text mustBe "How to register for an XI VAT number (opens in new tab)"
      XiVatRegisterDoc.body.getElementById("vat-register-link").attr(
        "href"
      ) mustBe "https://www.gov.uk/vat-registration/selling-or-moving-goods-in-northern-ireland"
    }

  }

}
