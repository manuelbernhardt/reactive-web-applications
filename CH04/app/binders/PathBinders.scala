package binders

import play.api.i18n.Lang
import play.api.mvc.PathBindable

object PathBinders {

  implicit object LangPathBindable extends PathBindable[Lang] {
    override def bind(key: String, value: String): Either[String, Lang] =
      Lang.get(value).toRight(s"Language $value is not recognized")

    override def unbind(key: String, value: Lang): String = value.code
  }

}
