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

package uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.RadioItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text

sealed trait ConfirmAddress

object ConfirmAddress extends Enumerable.Implicits {

  case object confirmedAddress extends WithName("confirmedAddress") with ConfirmAddress
  case object changeAddress    extends WithName("changeAddress") with ConfirmAddress
  case object enterManually    extends WithName("enterManually") with ConfirmAddress

  val values: Seq[ConfirmAddress] = Seq(confirmedAddress, changeAddress, enterManually)

  def mapValues(selectedValue: String): ConfirmAddress =
    selectedValue match {
      case "confirmedAddress" => confirmedAddress
      case "changeAddress"    => changeAddress
      case _                  => enterManually
    }

  def transformString(confirmDetails: ConfirmAddress): String =
    confirmDetails match {
      case ConfirmAddress.confirmedAddress => "confirmedAddress"
      case ConfirmAddress.changeAddress    => "changeAddress"
      case ConfirmAddress.enterManually    => "enterManually"
    }

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] = values.map {
    value =>
      RadioItem(
        value = Some(value.toString),
        content = Text(messages(s"pbe-confirm-address.${value.toString}")),
        checked = form("value").value.contains(value.toString)
      )
  }

  implicit val enumerable: Enumerable[ConfirmAddress] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
