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
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

sealed trait TradeWithNI

object TradeWithNI extends Enumerable.Implicits {

  case object Yes extends WithName("Yes") with TradeWithNI
  case object No  extends WithName("No") with TradeWithNI

  val values: Seq[TradeWithNI] = Seq(Yes, No)

  def fromBoolean(viewBalance: Boolean): TradeWithNI =
    if (viewBalance)
      Yes
    else
      No

  def options(form: Form[_])(implicit messages: Messages): Seq[RadioItem] =
    Seq(
      RadioItem(
        value = Some(Yes.toString),
        content = Text(messages(s"traderWithNI.${Yes.toString}")),
        checked = form("value").value.contains(Yes.toString)
      ),
      RadioItem(
        value = Some(No.toString),
        content = Text(messages(s"traderWithNI.${No.toString}")),
        checked = form("value").value.contains(No.toString)
      )
    )

  implicit val enumerable: Enumerable[TradeWithNI] =
    Enumerable(values.map(v => v.toString -> v): _*)

}