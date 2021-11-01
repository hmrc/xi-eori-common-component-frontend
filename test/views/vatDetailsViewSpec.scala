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
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  EstablishmentAddress,
  SubscriptionDisplayResponseDetail,
  SubscriptionInfoVatId
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.ConfirmDetailsViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.components.vat_details
import util.ViewSpec

import java.time.LocalDate

class vatDetailsViewSpec extends ViewSpec {

  private implicit val request = withFakeCSRF(fakeRegisterRequest)

  private val response = SubscriptionDisplayResponseDetail(
    Some("EN123456789012345"),
    "John Doe",
    EstablishmentAddress("house no Line 1", "city name", Some("SE28 1AA"), "ZZ"),
    Some(List(SubscriptionInfoVatId(Some("GB"), Some("999999")), SubscriptionInfoVatId(Some("ES"), Some("888888")))),
    Some("Doe"),
    Some(LocalDate.of(1963, 4, 1)),
    Some("XIE9XSDF10BCKEYAX")
  )

  private val viewWithoutXIVATModel = ConfirmDetailsViewModel(response, AffinityGroup.Organisation)

  private val viewWithXIVATModel =
    ConfirmDetailsViewModel(response.copy(XIVatNo = Some("XIVATNumber")), AffinityGroup.Organisation)

  private val vatDetailsWithXIVATView             = instanceOf[vat_details].apply(viewWithXIVATModel)
  private val vatDetailsWithoutXIVATView          = instanceOf[vat_details].apply(viewWithoutXIVATModel)
  private val vatDetailsDoc: Document             = Jsoup.parse(contentAsString(vatDetailsWithXIVATView))
  private val vatDetailsWithoutXIVATDoc: Document = Jsoup.parse(contentAsString(vatDetailsWithoutXIVATView))

  "Vat Details page " should {

    "show correct labels for users of type organisation " should {
      "display correct heading" in {
        vatDetailsDoc.body.getElementsByTag("h2").text mustBe "VAT details"
      }
      "display VAT number" in {
        val vatNumber = vatDetailsDoc.body.getElementsByClass("vat-number").get(0)
        vatNumber.getElementsByClass("govuk-summary-list__key").text mustBe "VAT number"
        vatNumber.getElementsByClass("govuk-summary-list__value").text mustBe "999999"
      }
      "display VAT registered address postcode" in {
        val vatNumber = vatDetailsDoc.body.getElementsByClass("postcode").get(0)
        vatNumber.getElementsByClass("govuk-summary-list__key").text mustBe "VAT registration address postcode"
        vatNumber.getElementsByClass("govuk-summary-list__value").text mustBe "DN18 5GP"
      }
      "display VAT effective date" in {
        val vatNumber = vatDetailsDoc.body.getElementsByClass("date").get(0)
        vatNumber.getElementsByClass("govuk-summary-list__key").text mustBe "VAT effective date"
        vatNumber.getElementsByClass("govuk-summary-list__value").text mustBe "3 April 1980"
      }
      "display XI VAT number if XI VAT number is present" in {
        val vatNumber = vatDetailsDoc.body.getElementsByClass("xi-vat-number").get(0)
        vatNumber.getElementsByClass("govuk-summary-list__key").text mustBe "XI VAT number"
        vatNumber.getElementsByClass("govuk-summary-list__value").text mustBe "XIVATNumber"
      }

      "display XI VAT register link if XI Vat number is not present" in {
        val vatNumber = vatDetailsWithoutXIVATDoc.body.getElementsByClass("xi-vat-number").get(0)
        vatNumber.getElementsByClass("govuk-summary-list__key").text mustBe "XI VAT number"
        vatNumber.getElementsByTag("a").attr(
          "href"
        ) mustBe uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiVatRegisterController.onPageLoad().url
      }
    }

  }

}
