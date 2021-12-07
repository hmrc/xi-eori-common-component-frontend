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
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.ConfirmDetailsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.ConfirmDetails.confirmedDetails
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.{
  EstablishmentAddress,
  SubscriptionDisplayResponseDetail,
  SubscriptionInfoVatId,
  XiSubscription
}
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.ConfirmDetailsViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.confirm_details
import util.ViewSpec

import java.time.LocalDate

class ConfirmDetailsViewSpec extends ViewSpec {

  private implicit val request: Request[AnyContentAsEmpty.type] = withFakeCSRF(fakeRegisterRequest)
  private val formProvider                                      = new ConfirmDetailsFormProvider().apply()
  private def form                                              = formProvider.bind(Map("value" -> confirmedDetails.toString))
  private def formWithError                                     = form.bind(Map("value" -> ""))
  val xiSubscription: XiSubscription                            = XiSubscription("XI8989989797", Some("7978"))

  private val response = SubscriptionDisplayResponseDetail(
    Some("EN123456789012345"),
    "John Doe",
    EstablishmentAddress("house no Line 1", "city name", Some("SE28 1AA"), "ZZ"),
    Some(List(SubscriptionInfoVatId(Some("GB"), Some("999999")), SubscriptionInfoVatId(Some("ES"), Some("888888")))),
    Some("Doe"),
    Some(LocalDate.of(1963, 4, 1)),
    Some(xiSubscription)
  )

  private val viewModelOrganisation                     = ConfirmDetailsViewModel(response, AffinityGroup.Organisation)
  private val confirmDetailsView                        = instanceOf[confirm_details].apply(form, viewModelOrganisation, None)
  private val confirmDetailsViewError                   = instanceOf[confirm_details].apply(formWithError, viewModelOrganisation, None)
  private lazy val confirmDetailsDoc: Document          = Jsoup.parse(contentAsString(confirmDetailsView))
  private lazy val confirmDetailsDocWithError: Document = Jsoup.parse(contentAsString(confirmDetailsViewError))

  "Confirm Details page " should {

    "display correct title" in {
      confirmDetailsDoc.title must startWith("Confirm details")
    }

    "display correct heading " in {
      confirmDetailsDoc.body.getElementsByTag("h1").text mustBe "Confirm details"
    }

    "display errors while empty form is submitted" in {
      confirmDetailsDocWithError.body.getElementsByClass("govuk-error-summary__list").get(
        0
      ).text mustBe "Tell us if these details are correct"
    }

  }

}
