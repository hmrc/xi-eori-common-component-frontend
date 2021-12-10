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

import org.jsoup.nodes.Document
import play.api.mvc.{AnyContentAsEmpty, Request}
import uk.gov.hmrc.xieoricommoncomponentfrontend.viewmodels.ConfirmContactDetailsViewModel
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.confirm_contact_details
import util.SpecData
import views.behaviours.ViewBehaviours

class ConfirmContactDetailsViewSpec extends ViewBehaviours with SpecData {

  private implicit val request: Request[AnyContentAsEmpty.type] = withFakeCSRF(fakeRegisterRequest)

  private val viewModel                   = ConfirmContactDetailsViewModel.fromContactInformation(contactInformation).get
  private val view                        = instanceOf[confirm_contact_details].apply(viewModel)
  private implicit lazy val doc: Document = asDocument(view)

  val headerText = "XI EORI application contact details"

  "Confirm Contact Details page " should {

    behave like normalPage(headerText)

    behave like pageWithBackLink

    "display a hint text" in {
      assertEqualsHtml(
        doc,
        "#confirm-contact-details-hint",
        "We will use these details to contact you about your XI EORI application."
      )
    }

    "display a Continue button with correct url" in {
      assertHasLink(doc, ".continue-button", "Continue", "")
    }

    "show correct contact details table labels" in {
      assertEqualsHtml(doc, ".name", "Contact full name")
      assertEqualsHtml(doc, ".email", "Contact email")
      assertEqualsHtml(doc, ".phone", "Contact phone")
      assertEqualsHtml(doc, ".address", "Contact address")
    }

    "show correct contact details table values" in {
      assertEqualsHtml(doc, ".name-value", "FirstName LastName")
      assertEqualsHtml(doc, ".email-value", "test@example.com")
      assertEqualsHtml(doc, ".phone-value", "1234567890")

      assertEqualsHtml(doc, ".address-street", "line 1")
      assertEqualsHtml(doc, ".address-city", "Newcastle")
      assertEqualsHtml(doc, ".address-post-code", "AA1 1AA")
      assertEqualsHtml(doc, ".address-country", "United Kingdom")
    }

    "show correct contact details table change buttons" in {
      assertHasLink(doc, ".name-change a", "Change", "/")
      assertHasLink(doc, ".email-change a", "Change", "/")
      assertHasLink(doc, ".phone-change a", "Change", "/")
      assertHasLink(
        doc,
        ".address-change a",
        "Change",
        uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.ContactAddressLookupController.onPageLoad().url
      )
    }
  }
}
