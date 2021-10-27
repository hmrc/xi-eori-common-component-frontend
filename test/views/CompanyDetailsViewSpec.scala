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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ConfirmDetailsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmDetails.confirmedDetails
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  EstablishmentAddress,
  SubscriptionDisplayResponseDetail,
  SubscriptionInfoVatId
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.ConfirmDetailsViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.components.company_details
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.confirm_details
import util.ViewSpec

import java.time.LocalDate

class CompanyDetailsViewSpec extends ViewSpec {

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

  private val viewModelOrganisation               = ConfirmDetailsViewModel(response, AffinityGroup.Organisation)
  private val viewModelIndividual                 = viewModelOrganisation.copy(affinityGroup = AffinityGroup.Individual)
  private val companyDetailsOrgView               = instanceOf[company_details].apply(viewModelOrganisation)
  private val companyDetailsIndView               = instanceOf[company_details].apply(viewModelIndividual)
  private val companyDetailsOrgDoc: Document      = Jsoup.parse(contentAsString(companyDetailsOrgView))
  private lazy val companyDetailsIndDoc: Document = Jsoup.parse(contentAsString(companyDetailsIndView))

  "Company Details page " should {

    "show correct labels for users of type organisation " should {
      "display correct heading" in {
        companyDetailsOrgDoc.body.getElementsByTag("h2").text mustBe "Company details"
      }
      "display registered company name" in {
        companyDetailsOrgDoc.body.getElementsByClass("name").text mustBe "Registered company name"
      }
      "display Registered company address" in {
        companyDetailsOrgDoc.body.getElementsByClass("address").text mustBe "Registered company address"
      }
      "display GB Eori number" in {
        companyDetailsOrgDoc.body.getElementsByClass("eori-number").text mustBe "GB EORI number"
      }
      "display date of establishment" in {
        companyDetailsOrgDoc.body.getElementsByClass("date").text mustBe "Date of establishment"
      }
      "display Corporation taxUTR" in {
        companyDetailsOrgDoc.body.getElementsByClass("utrOrNino").text mustBe "Corporation tax UTR"
      }
    }

    "show correct labels for users of type individual " should {
      "display correct heading" in {
        companyDetailsIndDoc.body.getElementsByTag("h2").text mustBe "Trader details"
      }
      "display full name" in {
        companyDetailsIndDoc.body.getElementsByClass("name").text mustBe "Full name"
      }
      "display Your address" in {
        companyDetailsIndDoc.body.getElementsByClass("address").text mustBe "Your address"
      }
      "display GB Eori number" in {
        companyDetailsIndDoc.body.getElementsByClass("eori-number").text mustBe "GB EORI number"
      }
      "display date of birth" in {
        companyDetailsIndDoc.body.getElementsByClass("date").text mustBe "Date of birth"
      }
      "display National insurance number" in {
        companyDetailsIndDoc.body.getElementsByClass("utrOrNino").text mustBe "National insurance number"
      }
    }

  }

}
