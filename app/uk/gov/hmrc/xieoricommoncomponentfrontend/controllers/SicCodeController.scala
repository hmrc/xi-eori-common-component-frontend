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

package uk.gov.hmrc.xieoricommoncomponentfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.mvc._
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.xieoricommoncomponentfrontend.cache.{SessionCache, UserAnswersCache}
import uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.auth.{AuthAction, EnrolmentExtractor}
import uk.gov.hmrc.xieoricommoncomponentfrontend.domain.LoggedInUserWithEnrolments
import uk.gov.hmrc.xieoricommoncomponentfrontend.forms.SicCodeFormProvider
import uk.gov.hmrc.xieoricommoncomponentfrontend.models.forms.SicCode
import uk.gov.hmrc.xieoricommoncomponentfrontend.views.html.{error_template, sic_code}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SicCodeController @Inject() (
  authAction: AuthAction,
  sicCodeView: sic_code,
  formProvider: SicCodeFormProvider,
  mcc: MessagesControllerComponents,
  errorTemplateView: error_template,
  userAnswersCache: UserAnswersCache,
  sessionCache: SessionCache
)(implicit val ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport with EnrolmentExtractor {

  private val form = formProvider()

  def onPageLoad: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction {
      implicit request => _: LoggedInUserWithEnrolments =>
        userAnswersCache.getSicCode map {
          case Some(code) =>
            Ok(sicCodeView(form.fill(SicCode(code))))
          case None => Ok(sicCodeView(form))
        }
    }

  def submit: Action[AnyContent] =
    authAction.ggAuthorisedUserWithEnrolmentsAction { implicit request => loggedInUser: LoggedInUserWithEnrolments =>
      form.bindFromRequest.fold(
        invalidForm => Future.successful(BadRequest(sicCodeView(invalidForm))),
        value =>
          loggedInUser.affinityGroup match {
            case Some(AffinityGroup.Organisation) =>
              sessionCache.subscriptionDisplay.flatMap {
                case Some(response) =>
                  userAnswersCache.cacheSicCode(value.sic).map(
                    _ => destinationsByNIPostCode(response.CDSEstablishmentAddress.postalCode)
                  )
                case None =>
                  Future.successful(
                    Redirect(
                      uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.LogoutController.displayTimeOutPage()
                    ).withNewSession
                  )
              }
            case _ =>
              Future.successful(
                Redirect(
                  uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded()
                )
              )

          }
      )
    }

  private def destinationsByNIPostCode(regPostcode: Option[String]): Result = regPostcode match {
    case Some(regPostcode) if !regPostcode.take(2).equalsIgnoreCase("BT") =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.HavePBEController.onPageLoad())
    case _ =>
      Redirect(uk.gov.hmrc.xieoricommoncomponentfrontend.controllers.routes.XiEoriNotNeededController.eoriNotNeeded())
  }

}
