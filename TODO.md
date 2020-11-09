# TODO

### RSENS

* Forcer le typage et éviter qu'on inject des scripts par exemple (int), (str) etc...
* Vérifier la valeur lorsque possible
* Convertir les caractères spéciaux. Permet d'éviter les injections de code (faille XSS) (htmlentities($_GET[param]))
* Requetes préparées pour plus de sécurité. Risque d'injection SQL, réutilisation, permet d'utiliser moins de ressource, s'éxécute plus vite et affranchissent la préoccupation des guillemets.