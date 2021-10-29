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
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.DisclosePersonalDetailsFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.disclose_personal_details
import util.ViewSpec

class DisclosePersonalDetailsViewSpec extends ViewSpec {

  private implicit val request = withFakeCSRF(fakeRegisterRequest)
  private val formProvider     = new DisclosePersonalDetailsFormProvider

  private def form = formProvider.apply()

  private def formWithError = form.bind(Map("value" -> ""))

  private val disclosePersonalDetailsView      = instanceOf[disclose_personal_details].apply(form)
  private val disclosePersonalDetailsViewError = instanceOf[disclose_personal_details].apply(formWithError)

  private lazy val disclosePersonalDetailsDoc: Document = Jsoup.parse(contentAsString(disclosePersonalDetailsView))

  private lazy val disclosePersonalDetailsDocWithError: Document =
    Jsoup.parse(contentAsString(disclosePersonalDetailsViewError))

  "You cannot use this service page for users of type standard org" should {

    "display correct title" in {
      disclosePersonalDetailsDoc.title must startWith(
        "Do you want to include the name and address on the EORI checker?"
      )
    }

    "display correct heading" in {
      disclosePersonalDetailsDoc.body.getElementsByTag(
        "h1"
      ).text mustBe "Do you want to include the name and address on the EORI checker?"
    }

    "display errors while empty form is submitted" in {
      disclosePersonalDetailsDocWithError.body.getElementsByClass("govuk-error-summary__list").get(
        0
      ).text mustBe "Tell us if you want to include the name and address on the EORI checker"
    }

    "display yes no radio buttons" in {

      disclosePersonalDetailsDoc.body.getElementById("value").attr("checked") mustBe empty
      disclosePersonalDetailsDoc.body.getElementById("value-2").attr("checked") mustBe empty
    }

    "display hint" in {
      disclosePersonalDetailsDoc.body.getElementById(
        "value-hint"
      ).text() mustBe "HMRC adds your XI EORI number to a public checker. You can also include your or your organisation’s name and address. Adding your EORI name and address to the checker will help customs and freight agents identify you and process your shipments quickly to minimise delays."
    }

  }

}
